#!/usr/bin/env python3
"""
Extract token usage and compute costs from Claude Code or Codex session JSONL.

Usage:
    python3 scripts/session-metrics.py [session_jsonl_path]
    python3 scripts/session-metrics.py --self-test

If no path is given, auto-discovers the session JSONL for the current working
directory. Claude Code logs are read from ~/.claude/projects. Codex rollout
logs are read from ~/.codex/sessions and ~/.codex/archived_sessions.

Configured pricing:
    Input:        $15.00 / 1M tokens
    Output:       $75.00 / 1M tokens
    Cache Write:  $3.75  / 1M tokens
    Cache Read:   $1.50  / 1M tokens
"""

import json
import sys
import glob
import os
import tempfile

# Claude Opus 4.6 pricing per 1M tokens
PRICE_INPUT = 15.00
PRICE_OUTPUT = 75.00
PRICE_CACHE_WRITE = 3.75
PRICE_CACHE_READ = 1.50


def project_relpath():
    """Return the home-relative CWD used by local session stores."""
    cwd = os.getcwd()
    home = os.path.expanduser("~")
    if cwd.startswith(home + os.sep):
        return cwd.replace(home + os.sep, "")
    return cwd.lstrip(os.sep)


def find_claude_session_jsonl():
    """Auto-discover a Claude Code session JSONL for the current working directory."""
    home = os.path.expanduser("~")
    rel = project_relpath().replace("/", "-")
    project_dir = os.path.join(home, ".claude", "projects", f"-{rel}")

    if not os.path.isdir(project_dir):
        for d in glob.glob(os.path.join(home, ".claude", "projects", "*")):
            if rel in os.path.basename(d):
                project_dir = d
                break

    files = glob.glob(os.path.join(project_dir, "*.jsonl"))
    # Exclude subagent files
    files = [f for f in files if "subagent" not in f and "agent-" not in os.path.basename(f)]
    if not files:
        return None
    return max(files, key=os.path.getmtime)


def read_codex_meta(jsonl_path, cwd):
    """Inspect enough Codex JSONL metadata to determine project and agent scope."""
    metadata = {
        "cwd_matches": False,
        "thread_source": None,
        "source": None,
        "is_subagent": False,
        "has_token_usage": False,
    }
    try:
        with open(jsonl_path) as f:
            for line in f:
                try:
                    d = json.loads(line)
                except json.JSONDecodeError:
                    continue
                payload = d.get("payload")
                if not isinstance(payload, dict):
                    continue
                if d.get("type") == "session_meta":
                    if payload.get("cwd") == cwd:
                        metadata["cwd_matches"] = True
                    metadata["thread_source"] = payload.get("thread_source")
                    metadata["source"] = payload.get("source")
                    if payload.get("thread_source") == "subagent":
                        metadata["is_subagent"] = True
                    source = payload.get("source")
                    if isinstance(source, dict) and "subagent" in source:
                        metadata["is_subagent"] = True
                elif payload.get("cwd") == cwd:
                    metadata["cwd_matches"] = True
                info = payload.get("info")
                if isinstance(info, dict) and "last_token_usage" in info:
                    metadata["has_token_usage"] = True
    except OSError:
        return metadata
    return metadata


def is_codex_subagent(metadata):
    """Return True when Codex metadata identifies a sub-agent rollout."""
    if metadata.get("is_subagent"):
        return True
    if metadata.get("thread_source") == "subagent":
        return True
    source = metadata.get("source")
    return isinstance(source, dict) and "subagent" in source


def find_codex_session_jsonl():
    """Auto-discover a Codex rollout JSONL for the current working directory."""
    home = os.path.expanduser("~")
    cwd = os.getcwd()
    roots = [
        os.path.join(home, ".codex", "sessions"),
        os.path.join(home, ".codex", "archived_sessions"),
    ]
    candidates = []
    for root in roots:
        for path in glob.glob(os.path.join(root, "**", "*.jsonl"), recursive=True):
            metadata = read_codex_meta(path, cwd)
            if not metadata["cwd_matches"] or not metadata["has_token_usage"]:
                continue
            candidates.append((is_codex_subagent(metadata), os.path.getmtime(path), path))
    if not candidates:
        return None
    # Prefer the newest non-subagent main rollout; fall back to subagent logs only
    # when no main rollout exists for this checkout.
    candidates.sort(key=lambda item: (not item[0], item[1]), reverse=True)
    return candidates[0][2]


def find_session_jsonl():
    """Auto-discover the best available session JSONL for the current checkout."""
    path = find_claude_session_jsonl()
    if path:
        return path
    path = find_codex_session_jsonl()
    if path:
        return path
    rel = project_relpath().replace("/", "-")
    print("No Claude or Codex session JSONL found for current working directory.")
    print(f"Checked Claude project slug: -{rel}")
    print("Checked Codex roots: ~/.codex/sessions and ~/.codex/archived_sessions")
    sys.exit(1)


def detect_format(jsonl_path):
    """Detect the local JSONL schema."""
    with open(jsonl_path) as f:
        for line in f:
            try:
                d = json.loads(line)
            except json.JSONDecodeError:
                continue
            if d.get("type") == "assistant":
                return "claude"
            payload = d.get("payload")
            if isinstance(payload, dict):
                if d.get("type") in {"session_meta", "event_msg", "response_item"}:
                    return "codex"
    return "unknown"


def extract_claude_usage(jsonl_path):
    """Extract all usage records from Claude assistant messages."""
    records = []
    with open(jsonl_path) as f:
        for line in f:
            try:
                d = json.loads(line)
            except json.JSONDecodeError:
                continue
            if d.get("type") != "assistant":
                continue
            msg = d.get("message", {})
            if not isinstance(msg, dict):
                continue
            usage = msg.get("usage", {})
            inp = usage.get("input_tokens", 0)
            out = usage.get("output_tokens", 0)
            cache_create = usage.get("cache_creation_input_tokens", 0)
            cache_read = usage.get("cache_read_input_tokens", 0)
            if inp > 0 or out > 0 or cache_create > 0 or cache_read > 0:
                records.append({
                    "input": inp,
                    "output": out,
                    "cache_create": cache_create,
                    "cache_read": cache_read,
                })
    return records


def extract_codex_usage(jsonl_path):
    """Extract all token_count usage records from Codex rollout JSONL."""
    records = []
    with open(jsonl_path) as f:
        for line in f:
            try:
                d = json.loads(line)
            except json.JSONDecodeError:
                continue
            payload = d.get("payload")
            if not isinstance(payload, dict) or payload.get("type") != "token_count":
                continue
            info = payload.get("info")
            if not isinstance(info, dict):
                continue
            usage = info.get("last_token_usage")
            if not isinstance(usage, dict):
                continue
            inp_total = usage.get("input_tokens", 0)
            cache_read = usage.get("cached_input_tokens", 0)
            cache_create = usage.get("cache_write_input_tokens", 0)
            out = usage.get("output_tokens", 0)
            inp = max(inp_total - cache_read - cache_create, 0)
            if inp > 0 or out > 0 or cache_create > 0 or cache_read > 0:
                records.append({
                    "input": inp,
                    "output": out,
                    "cache_create": cache_create,
                    "cache_read": cache_read,
                })
    return records


def extract_usage(jsonl_path, log_format=None):
    """Extract all usage records from a supported session JSONL format."""
    log_format = log_format or detect_format(jsonl_path)
    if log_format == "claude":
        return extract_claude_usage(jsonl_path)
    if log_format == "codex":
        return extract_codex_usage(jsonl_path)
    raise ValueError(f"Unsupported session JSONL format: {jsonl_path}")


def compute_cost(tokens, price_per_million):
    return tokens * price_per_million / 1_000_000


def run_self_test():
    """Exercise Claude and Codex parsing with synthetic JSONL records."""
    with tempfile.TemporaryDirectory() as tmp:
        claude_path = os.path.join(tmp, "claude.jsonl")
        codex_path = os.path.join(tmp, "codex.jsonl")
        with open(claude_path, "w") as f:
            f.write(json.dumps({
                "type": "assistant",
                "message": {
                    "usage": {
                        "input_tokens": 10,
                        "output_tokens": 4,
                        "cache_creation_input_tokens": 2,
                        "cache_read_input_tokens": 3,
                    }
                },
            }) + "\n")
        with open(codex_path, "w") as f:
            f.write(json.dumps({
                "type": "session_meta",
                "payload": {
                    "cwd": os.getcwd(),
                    "thread_source": "subagent",
                    "source": {
                        "subagent": {
                            "thread_spawn": {
                                "parent_thread_id": "parent",
                                "depth": 1,
                            }
                        }
                    },
                },
            }) + "\n")
            f.write(json.dumps({
                "type": "session_meta",
                "payload": {
                    "cwd": os.getcwd(),
                    "source": "vscode",
                },
            }) + "\n")
            f.write(json.dumps({
                "type": "event_msg",
                "payload": {
                    "type": "token_count",
                    "info": {
                        "last_token_usage": {
                            "input_tokens": 100,
                            "cached_input_tokens": 40,
                            "cache_write_input_tokens": 10,
                            "output_tokens": 5,
                            "total_tokens": 105,
                        }
                    },
                },
            }) + "\n")
        claude_records = extract_usage(claude_path)
        codex_records = extract_usage(codex_path)
        assert claude_records == [{
            "input": 10,
            "output": 4,
            "cache_create": 2,
            "cache_read": 3,
        }]
        assert codex_records == [{
            "input": 50,
            "output": 5,
            "cache_create": 10,
            "cache_read": 40,
        }]
        assert is_codex_subagent(read_codex_meta(codex_path, os.getcwd()))
    print("SELF-TEST PASS")


def main():
    if len(sys.argv) > 1 and sys.argv[1] == "--self-test":
        run_self_test()
        return

    if len(sys.argv) > 1:
        jsonl_path = sys.argv[1]
    else:
        jsonl_path = find_session_jsonl()

    log_format = detect_format(jsonl_path)
    print(f"Session: {os.path.basename(jsonl_path)}")
    print(f"Format: {log_format}")
    print()

    records = extract_usage(jsonl_path, log_format)

    total_in = sum(r["input"] for r in records)
    total_out = sum(r["output"] for r in records)
    total_cache_create = sum(r["cache_create"] for r in records)
    total_cache_read = sum(r["cache_read"] for r in records)

    cost_in = compute_cost(total_in, PRICE_INPUT)
    cost_out = compute_cost(total_out, PRICE_OUTPUT)
    cost_cache_create = compute_cost(total_cache_create, PRICE_CACHE_WRITE)
    cost_cache_read = compute_cost(total_cache_read, PRICE_CACHE_READ)
    total_cost = cost_in + cost_out + cost_cache_create + cost_cache_read

    print(f"{'Category':<25} {'Tokens':>14} {'Cost':>10}")
    print(f"{'-'*25} {'-'*14} {'-'*10}")
    print(f"{'Input':<25} {total_in:>14,} ${cost_in:>8.2f}")
    print(f"{'Output':<25} {total_out:>14,} ${cost_out:>8.2f}")
    print(f"{'Cache Write':<25} {total_cache_create:>14,} ${cost_cache_create:>8.2f}")
    print(f"{'Cache Read':<25} {total_cache_read:>14,} ${cost_cache_read:>8.2f}")
    print(f"{'-'*25} {'-'*14} {'-'*10}")
    print(f"{'TOTAL':<25} {total_in+total_out+total_cache_create+total_cache_read:>14,} ${total_cost:>8.2f}")
    print()
    print(f"API calls (usage records): {len(records)}")


if __name__ == "__main__":
    main()
