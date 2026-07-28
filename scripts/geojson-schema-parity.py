#!/usr/bin/env python3
"""Compare eight bundled GeoJSON ATS schemas with the pinned released source."""

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
    parity.SELF_TEST_LABEL = "GeoJSON"
    parity.ENTRY_SCHEMAS = (
        "connected-systems-1/geojson/system.json",
        "connected-systems-1/geojson/systemCollection.json",
        "connected-systems-1/geojson/deployment.json",
        "connected-systems-1/geojson/deploymentCollection.json",
        "connected-systems-1/geojson/procedure.json",
        "connected-systems-1/geojson/procedureCollection.json",
        "connected-systems-1/geojson/samplingFeature.json",
        "connected-systems-1/geojson/samplingFeatureCollection.json",
    )
    return parity.main()


if __name__ == "__main__":
    raise SystemExit(main())
