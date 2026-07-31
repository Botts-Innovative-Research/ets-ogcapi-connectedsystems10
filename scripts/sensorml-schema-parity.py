#!/usr/bin/env python3
"""Compare eight bundled SensorML ATS schemas with the pinned released source."""

from __future__ import annotations

import importlib.util
from pathlib import Path


def load_parity_module():
    source = Path(__file__).with_name("property-schema-parity.py")
    spec = importlib.util.spec_from_file_location("schema_parity", source)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"could not load parity support from {source}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def main() -> int:
    parity = load_parity_module()
    parity.SELF_TEST_LABEL = "SensorML"
    parity.ENTRY_SCHEMAS = (
        "connected-systems-1/sensorml/system.json",
        "connected-systems-1/sensorml/systemCollection.json",
        "connected-systems-1/sensorml/deployment.json",
        "connected-systems-1/sensorml/deploymentCollection.json",
        "connected-systems-1/sensorml/procedure.json",
        "connected-systems-1/sensorml/procedureCollection.json",
        "connected-systems-1/sensorml/property.json",
        "connected-systems-1/sensorml/propertyCollection.json",
    )
    return parity.main()


if __name__ == "__main__":
    raise SystemExit(main())
