# DGIWG Glaux

Welcome to the central home of **DGIWG Glaux**, an open-source suite of reference implementations and foundational software engineered to operationalize **NATO STANAG 4789** (Sensor Integration Standard for JISR Operations).

Developed under the auspices of the Defense Geospatial Information Working Group (DGIWG) Technical Panel 5, Project 07 (Sensor Web Enablement), Glaux utilizes the **OGC API - Connected Systems** architecture to provide the official resources, reference deployments, evaluation tools, and technical guidance necessary to accelerate plug-and-play sensor integration and drive seamless, secure data interoperability across the Alliance and its coalition partners.

---

## 🌐 Project Website

For an overview of the Glaux software suite, vision, and deployment guidance, visit the project homepage:

**<a href="https://dgiwg-p507.github.io/glaux/" target="_blank" rel="noopener noreferrer">https://dgiwg-p507.github.io/glaux/</a>**

---

## 🏛️ The Glaux Ecosystem

DGIWG Glaux is maintained as a modular ecosystem of specialized components. This repository is the **meta-repository** and central source of truth for architecture, documentation, and orchestration policy across independent codebases.

### Core Infrastructure

* **[Glaux (Meta-Repo)](../glaux)** - The flagship landing page and documentation hub, coordinating ecosystem orchestration through release-tag alignment and shared integration manifests.
* **[Glaux Server](../glaux-server)** - The foundational OGC API - Connected Systems authority node for secure discovery, observations, and tasking workflows.

### Data and Simulation

* **[Glaux Simulator](../glaux-simulator)** - The STANAG 4789 telemetry generator for validation, stress-testing, and demonstrations without live tactical feeds.
* **[Glaux Publisher](../glaux-publisher)** - The ingestion bridge that translates legacy or non-standard sensor feeds into compliant interoperable streams.

### Visualization Clients

* **[Glaux Web App](../glaux-webapp)** - The browser-based operational dashboard for monitoring and visualization of connected platforms and sensor states.
* **[Glaux Mobile](../glaux-mobile)** - The cross-platform Android/iOS tactical-edge client for field-level situational awareness and sensor interaction.

### Functional Summary

| Component | Primary Role | Stakeholder Value |
| --- | --- | --- |
| Meta-Repo | Orchestration | Unified documentation and integration baseline across the suite |
| Server | Authority | Conformance-harness-backed interoperability for OGC/NATO workflows |
| Simulator | Validation | Rapid prototyping without real-world hardware |
| Publisher | Integration | Bridges legacy systems to modern compliant streams |
| Visualization Clients (Web/Mobile) | Visualization | Turns raw sensor data into actionable operational intelligence |

---

## ✅ Conformance

Glaux Server compliance claims are validated through an explicit **conformance test harness** and implementation profiles aligned to OGC and NATO interoperability requirements.

---

## 📅 Governance & Context

Conceived in Athens, Greece, during the DGIWG Spring 2026 plenary sessions, the project draws its name from the classical Athenian Glaux ($\gamma \lambda \alpha \acute{\upsilon} \xi$)—the Little Owl of Athena. Symbolizing acute perception, strategic intelligence, and the ability to see clearly through darkness, Glaux represents the project's mission to bring absolute clarity, interoperability, and vision to modern Joint ISR sensor networks.

---
