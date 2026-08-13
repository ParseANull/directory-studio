<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Modernization Roadmap: Eclipse 2026-06 + Java 25

Tracking document for the `release/2.0.0-eclipse2026-java25` branch. Captures the
current-state audit, target-state decisions, and phased plan agreed on before
any code changes were made.

## Goal

Run Apache Directory Studio on the current 2026 Eclipse Simultaneous Release
(2026-06, Platform 4.40) with Java 25 as the compiler target and host JVM.

## Current state vs. target

| Component | Pinned today | Target |
|---|---|---|
| Eclipse Platform | 4.24 (2022-06) | 4.40 (2026-06) |
| Tycho | 2.3.0 (2021) | 5.0.3 (2026-05-29) |
| Java compiler level | 11 (Tycho side), 1.8 (plain-Maven side) | 25 |
| Orbit | `tools/orbit/downloads/2020-12` (http) | `tools/orbit/simrel/orbit-aggregation/2026-06` |
| SWTBot | 3.0.0 (2020) | latest (4.1.0) |
| Babel NLS (DE/FR platform chrome) | Oxygen, R0.15.1 (2017) | **dropped** (see Decisions) |
| Target OS/arch | linux-x64, win32-x64, macOS x64+aarch64 | unchanged |
| Manifest generation | two-pass: `pom-first.xml` (Felix `maven-bundle-plugin`) then Tycho | single-pass, Tycho-native |

## Relevant prior art upstream

- [DIRSTUDIO-1309](https://issues.apache.org/jira/browse/DIRSTUDIO-1309): Eclipse 4.24
  was deliberately chosen as the last version compatible with Java 11; maintainer
  noted Java 17 would be needed to move further. That bump never happened.
- [apache/directory-studio#43](https://github.com/apache/directory-studio/pull/43):
  stepping to Eclipse 4.25 broke on a JUnit dependency conflict in the target
  platform. Tycho 5.x overhauled JUnit provider handling, and we're bumping
  SWTBot/Orbit too, so this specific trap shouldn't reproduce — verify during
  Phase 3 regardless.
- [eclipse-tycho/tycho#5516](https://github.com/eclipse-tycho/tycho/issues/5516):
  Tycho 5.0.0's bundled ECJ initially rejected `release=25` (filed before Tycho's
  ECJ was aligned to the 2025-09 train). Should be resolved by 5.0.3; fallback is
  `<compilerId>javac</compilerId>` (officially supported by Tycho 5.x), which
  delegates to the host JDK 25 `javac` directly.

## Decisions (agreed 2026-08-12)

1. **Babel NLS: dropped entirely.** Only affects translation of generic Eclipse
   platform chrome (File/Window/etc menus). Directory Studio's own bundled
   `plugin_de.properties` / `plugin_fr.properties` (its own UI strings) are
   unrelated and unaffected.
2. **Manifest generation: migrate to Tycho-native.** Retire `build.sh`'s two-pass
   build (`mvn -f pom-first.xml clean install && mvn clean install`) and the
   Felix `maven-bundle-plugin` pre-pass across all plugin modules, in favor of
   Tycho 5.x's built-in bnd-based manifest generation, collapsing to a single
   `mvn clean install`. Exact wiring (per-module `bnd.bnd` vs. inline
   instructions) to be confirmed against current Tycho 5.0.3 docs during
   execution.
3. **linux-aarch64: not adding.** Keep the current 4 build environments.

## Phased plan

- **Phase 1 — Toolchain bump (isolated from target-platform changes)**
  - Bump `tycho.version` 2.3.0 → 5.0.3 (all poms/pluginManagement)
  - Bump compiler source/target/release → 25 (align `maven.compiler.*`
    properties with `tycho-compiler-plugin` config; prefer `<release>`)
  - Retire `pom-first.xml` / `build.sh` two-pass; migrate manifest generation
    to Tycho-native, module by module (~30 modules)
  - Validate: full build green on JDK 25 + Tycho 5.0.3 while *still* pointed at
    the old Eclipse 4.24 target — isolates toolchain risk from target-platform
    risk

- **Phase 2 — Target platform rewrite**
  - Point Platform/RCP/JDT/PDE/Equinox units at
    `download.eclipse.org/releases/2026-06/` (use Tycho 5.x's unversioned/
    range `<unit>` support instead of hand-pinned qualifiers)
  - Orbit → `orbit-aggregation/2026-06` (and `orbit-legacy/release/4.40.0` for
    wrapped legacy bundles like `javax.xml.stream`, `slf4j`)
  - SWTBot → `releases/latest` (4.1.0)
  - Remove Babel location block entirely

- **Phase 3 — Compile & fix fallout**
  - Full two-pass-turned-one-pass build against new target + Tycho 5.x +
    Java 25
  - Work through ~4 years of deprecated/removed Platform UI API
  - Watch for the historical JUnit trap (DIRSTUDIO-1309/PR#43 context above)
  - Fallback to `compilerId=javac` if the bundled ECJ rejects `release=25`

- **Phase 4 — Runtime validation**
  - Materialize product per OS/arch, manual smoke test (launch, LDAP connect,
    schema browser)
  - Run SWTBot UI suite (`tests/test.integration.ui`) — biggest open unknown,
    since SWTBot's own docs only claim verified compatibility through 2023-06
  - Run `tests/test.integration.core` against the Docker test lab (OpenLDAP,
    389ds, Kerby)

- **Phase 5 — Packaging**
  - Confirm NSIS Windows installer and macOS packaging against new product
    output
  - Consider Tycho 5.x's `includeJRE` product flag to ship a bundled JDK 25
    JRE

- **Phase 6 — CI**
  - `Jenkinsfile` only has jdk-11/jdk-17 Docker lanes; add jdk-25 (or stand up
    GitHub Actions on this fork instead)

- **Phase 7 — Docs & release**
  - Update stated prerequisites (README, AGENTS.md)
  - Tag release

## Explicitly out of scope

Bumping pinned third-party library versions (BouncyCastle 1.62, httpclient
4.5.12, commons-io 2.6, slf4j 1.7.25, etc.) is a separate initiative. Old
bytecode runs fine on JDK 25 regardless of compile-time version, so none of
these block this migration — they're flagged as a future hygiene pass, not
addressed here.

## Progress log

**2026-08-13 — Phase 1 complete (toolchain), pending real-build validation.**

- `tycho.version` 2.3.0 → 5.0.3; `maven.compiler.release` → 25 on both
  `tycho-compiler-plugin` and `maven-compiler-plugin`. Done in isolation from
  the target-platform bump (Phase 2), which hasn't started — still pointed at
  Eclipse 4.24.
- Manifest generation migrated off the `pom-first.xml`/Felix `maven-bundle-plugin`
  two-pass hack. All 31 plugin/help/test modules now have a static, checked-in
  `META-INF/MANIFEST.MF`, mechanically derived from each module's old
  `pom-first.xml` `<instructions>` block (verified beforehand: every
  Export-Package/Import-Package/Require-Bundle in this project was already
  fully hand-declared, never a bnd bytecode-scanning wildcard — safe to do
  without a live build). Maven `${...}` property placeholders (bundle-version
  pins) resolved to literal values from the top-level `pom.xml`.
- `eclipse-trgt-platform/pom-first.xml` (builds a local p2 repo for
  third-party library jars — unrelated mechanism) was deliberately left
  alone. `build.sh` is still a two-invocation build, just for that narrower
  reason now.
- **Not yet validated against a real build** — this sandbox has no network
  access to Maven Central or Eclipse's p2 repositories, so none of Phase 1
  has been compiled/tested for real yet. Everything above is correct by
  construction (mechanical transformation from already-known, explicit
  data) and by inspection, not by a passing build.
- Deferred: `RELEASE.md`'s version-bump step for `pom-first.xml` files will
  now only touch 2 files instead of 33; likely fine since
  `tycho-versions-plugin:set-version` (used later in the same doc) handles
  `MANIFEST.MF` Bundle-Version bumps automatically, but unverified.

Next: Phase 2 (target platform rewrite — Eclipse 4.24 → 2026-06, Orbit,
SWTBot, drop Babel).
