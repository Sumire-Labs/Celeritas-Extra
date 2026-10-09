# CHANGELOG

All notable changes to Celeritas Extra are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Release dates use Japan Standard Time (UTC+09:00). Entries are based on the
[published GitHub release notes](https://github.com/Sumire-Labs/Celeritas-Extra/releases)
and tagged commit history.

## [0.9.2] - Unreleased

### Added

- Added a complete historical changelog covering releases from 0.1.0 through 0.9.1.

### Changed

- Updated the build to CleanroomModTemplate's current Mixin branch while retaining renderer adapters and release artifact checks.
- Updated Gradle to 9.8.0, Unimined to 1.4.43-kappa, and Shadow to 9.6.1.
- Updated the development loader to Cleanroom 0.6.13-alpha.
- Replaced Blossom with TokenEnvoy 3.0.0 for compile-time constant and resource token replacement. Moved `Reference.java`, `mcmod.info`, and `pack.mcmeta` from template directories into the standard source and resource directories.
- Adopted Cleanroom Versioning 3.3.0 to derive versions from Git tags and branch history instead of the manual `mod_version` property.
- Updated GitHub Actions to fetch full history and tags for version calculation.
- Extended release Jar verification to check resource-pack metadata, compiled build constants, and the inlined Forge `@Mod` annotation against the computed version.

### Fixed

- Persisted Cleanroom's borderless fullscreen preference when changing screen modes, preventing the preference from reverting after restarting the game.

## [0.9.1] - 2026-10-08

### Fixed

- Preserved the Shader Packs tab when using Demonica with Extra's searchable video settings.
- Retained native action-tab callbacks, including opening the shader pack selection screen, during filtering and mod-category folding.

## [0.9.0] - 2026-10-07

### Added

- Nothirium support, including RenderLib-aware entity render distance limits.
- A vanilla-style Extra settings screen with two-column buttons, sliders, category submenus, tooltips, and reset controls.
- Pintonium compatibility, with an Extra Settings button in the top-left corner of its video screen while preserving its own search and shader controls.

### Changed

- Modern Clouds override resource-pack cloud textures while enabled; disabling the option restores the resource-pack texture.
- Moved Java packages to `com.sumirelabs`; existing configuration files remain compatible.

### Fixed

- Fixed a texture animation Mixin conflict with LoliASM's on-demand animation system.
- Fixed F3 cache handling to prevent duplicate renderer information with Nothirium and RenderLib.
- Fixed a crash when opening Pintonium's settings screen ([#37](https://github.com/Sumire-Labs/Celeritas-Extra/issues/37)).

### Requirements

- Cleanroom Loader 0.6.10-alpha or newer and Java 25.
- Exactly one renderer: Celeritas, Actinium, or Nothirium.
- Nothirium requires RenderLib; Nothirium 0.4.9-beta or newer and RenderLib 1.4.5 are recommended.
- AssetMover 2.5 or newer is optional and required to download the Modern Clouds texture.

## [0.8.0] - 2026-10-05

### Added

- Actinium support with automatic renderer detection and shared configuration.
- Settings search and filtering, the Ctrl+F shortcut, and Shift + scroll slider adjustment.
- Collapsible mod categories in the Celeritas settings sidebar.
- Memory usage overlay.
- Sign text and item-frame map back-face culling.
- Configurable entity and tile entity render distances.
- Per-particle spawn percentages.
- Separate sun and moon toggles.
- Per-dimension fog settings.
- Menu, unfocused, and minimized FPS limits.

### Changed

- FPS statistics display average FPS, 1% low, and 0.1% low.
- Reorganized settings into ten category pages.
- Updated localization and aligned translation keys with upstream projects.
- Actinium uses its integrated settings frontend, search, and mod-category folding.
- Duplicate cloud and menu FPS controls are hidden when using Actinium.

### Fixed

- Corrected item-frame LOD, including Forge emissive rendering and Actinium's fast item rendering path.
- Fixed crashes when adjusting sliders with Shift + scroll.
- Preserved pending settings while searching or collapsing mod categories.
- Prevented camera-based culling and fog overrides from affecting Iris shadow passes.
- Fixed the search cursor position during text selection.
- Fixed FPS statistics with negative monotonic clock timestamps.
- Fixed early configuration loading to use the correct game directory.

### Requirements

- Cleanroom Loader, Java 25, and either Celeritas or Actinium.

## [0.7.1] - 2026-09-07

### Changed

- Reworked Modern Clouds into a texture-only backport using a built-in resource pack and the existing cloud renderer.
- Preserved cloud height, render distance, and scale controls while giving user resource packs priority.
- Applying the Modern Clouds option reloads resources.

### Fixed

- Fixed Modern Clouds remaining unavailable when checked before AssetMover finished downloading its texture.
- Kept the standard fullscreen option on Android/Pojav-based launchers and incompatible GLFW runtimes to avoid unsupported borderless window controls.

### Removed

- Removed the custom circular cloud rendering and distance fade introduced in 0.7.0.

## [0.7.0] - 2026-08-08

### Added

- Backported Minecraft 1.21.6-style Modern Clouds, with custom circular cloud rendering and distance fade.
- Optional AssetMover integration to acquire the modern cloud texture.

### Changed

- Refactored the implementation and rewrote the README.

### Removed

- Removed the HEI hide-until-search feature.

## [0.6.3] - 2026-07-23

### Removed

- Removed the unused Reduced Motion option.

## [0.6.2] - 2026-07-23

### Changed

- Replaced the advanced item tooltip option with an option to display the item's source mod name.

### Fixed

- Fixed an `EntityRenderer` startup crash reported with Celeritas ([#16](https://github.com/Sumire-Labs/Celeritas-Extra/issues/16)).

## [0.6.1] - 2026-07-03

### Added

- Advanced item tooltips in JEI/HEI.

## [0.6.0] - 2026-07-02

### Added

- Sky Colors toggle to enable or disable biome-based sky coloring.
- Advanced Item Tooltips option to show an item's ID and durability without F3+H.
- Master toast toggle and individual controls for advancement, recipe, tutorial, and system notifications.
- Item Frame LOD Distance setting, from 0 to 256 blocks, with 0 disabling LOD. Beyond the selected distance, framed 3D block items render only their front/back faces and framed maps are hidden, while frame borders and name tags remain visible.

### Changed

- Cloud Scale displays a 0.25x-1.00x multiplier instead of the internal 1-4 value.
- Dependent controls disable live when their parent setting is disabled, including fog, clouds, particles, animations, extended FPS, steady-HUD refresh interval, beacon beam height, item-frame name tags, and star count.
- Added block/tick unit labels and performance-impact hints for cloud distance and star count.
- Simplified fog controls to a single Fog toggle, removing the redundant Fog Type selector.
- Corrected inaccurate and inconsistent translations.

### Fixed

- Fog toggles and distance settings preserve blindness, underwater, and lava fog.
- The hidden HEI item list no longer draws, displays tooltips, or opens recipes when clicking its empty area.

## [0.5.1] - 2026-07-01

### Changed

- Reworked cloud distance and fog settings to render extended clouds correctly, with a small performance improvement.
- Expanded the maximum cloud render distance from 64 to 128.

### Fixed

- Fixed the white-out fog issue.

## [0.5.0] - 2026-07-01

### Changed

- Discover particle classes when a world loads so their controls are available before each particle has spawned.
- Group discovered particles by owning mod, cache them between sessions, and prune the cache when a mod is removed.
- Retain first-spawn discovery as a fallback for lambda and anonymously registered particle factories.
- Guard repeated per-particle work by class identity, avoiding repeated reflection and string allocation for known classes.
- Switched Gradle scripts to native property access and removed `helpers.gradle`.
- Updated Gradle to 9.6.1.

### Fixed

- Fixed missing per-class controls, especially for modded particles.
- Corrected particle mod attribution instead of grouping all particles under "unknown".
- Prevented particle-page construction failures from breaking the rest of the settings screen.

### Removed

- Removed the redundant firework particle Mixin.
- Stopped generating sources and Javadoc Jars.

## [0.4.6] - 2026-07-01

### Removed

- Removed the `COCOA_RETINA_FRAMEBUFFER` option.

## [0.4.5] - 2026-04-11

### Fixed

- Fixed a crash on devices that do not support adaptive VSync ([#12](https://github.com/Sumire-Labs/Celeritas-Extra/issues/12)).

## [0.4.4] - 2026-04-09

### Changed

- Updated the Maven URL.

### Fixed

- Fixed a star VBO `NullPointerException` in sky rendering ([#11](https://github.com/Sumire-Labs/Celeritas-Extra/issues/11)).

## [0.4.3] - 2026-04-07

### Added

- Cloud scale setting.
- Configurable star count.
- Cloud translucency setting.
- Void particle and void fog toggles.

## [0.4.2] - 2026-04-07

### Added

- Adjustable cloud render distance.

### Fixed

- Fixed particle detection failing to detect particles at startup.

## [0.4.1] - 2026-04-06

### Changed

- Updated Cleanroom Loader from 0.5.6-alpha to 0.5.7-alpha.

### Fixed

- Suppressed tooltips while HEI's item list is hidden and centered the search hint within the grid area.

## [0.4.0] - 2026-04-06

### Added

- FPS 1% low and 0.1% low metrics using a time-based five-second rolling window, replacing maximum/minimum FPS.
- Three-way Windowed, Borderless, and Fullscreen selection through Cleanroom's window API.
- Hide HEI Until Searching option, with a "Type to search..." hint and automatic hiding of the option when HEI is absent.
- macOS Retina Framebuffer option using Cleanroom's `COCOA_RETINA_FRAMEBUFFER` setting, disabled on other platforms.
- Performance-impact indicators for all animations, all particles, and light updates.

### Changed

- Simplified particle detection to ASM-only scanning, removing the multi-strategy `scanFactories()` implementation and `IMixinParticleManager`.
- Particle display names use the `ParticleFlame (minecraft)` format.
- Migrated the toolchain and source/target compatibility to Java 25 and Cleanroom 0.5.x.
- Updated Unimined from 1.4.15-kappa to 1.4.17-kappa and Cleanroom Loader from 0.3.35-alpha to 0.5.6-alpha.
- Added HEI as an optional compile-time dependency for Mixin compatibility.
- Modernized Java code with switch expressions, records, `var`, and pattern matching.
- Refactored FPS tracking, option helpers, constants, and screen-mode/VSync state handling.

### Fixed

- Localized Screen Mode, Overlay Corner, Text Contrast, and VSync selector values.
- Removed literal newline escape sequences from tooltips in all four languages.

## [0.3.2] - 2026-03-24

### Added

- Adaptive VSync support with Off, On, and Adaptive modes, ported from Sodium Extra.
- GLX/WGL swap-control-tear detection and fallback on unsupported drivers.

### Changed

- Updated Gradle to 9.4.1, Unimined to 1.4.15-kappa, Shadow to 9.4.0, and JUnit Jupiter to 6.0.3.
- Updated Actions checkout to 6.0.2, setup-java to 5.2.0, setup-gradle to 5.0.2, upload-artifact to 7.0.0, and release-action to 1.21.0.

## [0.3.1] - 2026-03-14

### Fixed

- Fixed a crash with LoliASM by moving `ProfilerHelper` out of the Mixin package.

## [0.3.0] - 2026-03-14

### Added

- Runtime class-based particle discovery and per-class toggles grouped by mod, replacing the static particle-ID filter.

### Changed

- Unified configuration reading and writing through `BooleanProperty` and `IntProperty` bindings.
- Extracted option-page boilerplate into `booleanOption` and `sliderOption` helpers.
- Replaced biome color magic numbers with named constants.
- Shared profiler logic through `ProfilerHelper`.
- Renamed ambiguous entity renderer Mixins to distinguish weather rendering and shader prevention.
- Consolidated mod ID, name, and version into `Reference`.
- Migrated to the updated CleanroomModTemplate Mixin branch and pinned Cleanroom Loader to 0.3.35-alpha.

### Fixed

- Fixed the screen turning white when fog was disabled.

## [0.2.4] - 2026-02-02

### Changed

- Refactored the codebase and cleaned up imports.

## [0.2.3] - 2026-01-31

### Changed

- Updated Unimined to 1.4.10 and Cleanroom Loader to 0.3.34.
- Enabled Javadoc Jar generation.

### Removed

- Removed built-in leaf culling to support [CeleritasLeafCulling](https://github.com/Karnatour/CeleritasLeafCulling).

## [0.2.2] - 2026-01-19

### Added

- Lightmap option.

## [0.2.1] - 2026-01-15

### Changed

- Updated Russian localization by @Ar2t1e ([#7](https://github.com/Sumire-Labs/Celeritas-Extra/pull/7)).

## [0.2.0] - 2026-01-15

### Added

- Leaf culling with Fast, Balanced, Quality, and Custom presets.
- Beacon beam height limit.
- Extended maximum/average/minimum FPS display.
- Overlay text contrast setting.
- Ignore Reduced Debug Info option.
- Fog distance slider and Fog toggle.
- Particle options backported from Rubidium Extra for combat effects, spells, environmental effects, decorative particles, miscellaneous particles, and mob effects.

### Changed

- Migrated configuration storage from JSON to Forge `.cfg` files.
- Optimized the icon size and Maven configuration.

## [0.1.14] - 2026-01-05

### Fixed

- Fixed a crash with the latest Celeritas version.

## [0.1.13] - 2026-01-02

### Removed

- Removed unused files.
- Removed the Instant Sneak option because it is redundant in Minecraft 1.12.

## [0.1.12] - 2026-01-02

### Fixed

- Fixed incorrect mod ID and logo display.

## [0.1.11] - 2025-12-29

### Changed

- Disabled detailed particle options whose implementation was postponed because of Mixin issues.

### Fixed

- Restored the correct version reference information.

## [0.1.10] - 2025-12-26

### Changed

- Raised the required Cleanroom Loader version from 0.3.27 to 0.3.31.

## [0.1.9] - 2025-12-24

### Added

- GitHub project URL in mod metadata.
- New mod icon.

## [0.1.8] - 2025-12-24

### Changed

- Updated Shadow to 9.3.0 and Blossom to 2.2.0.
- Minor code cleanup.

## [0.1.7] - 2025-12-03

### Changed

- Updated Gradle to 9.2.1 and sponge-mixin to `0.20.12+mixin.0.8.7`.
- Updated Actions checkout to 6.0.0, upload-artifact to 5.0.0, and release-action to 1.20.0.
- Updated the Mixin property name.

## [0.1.6] - 2025-11-23

### Added

- Thai localization (`th_th.lang`).

## [0.1.5] - 2025-11-07

### Added

- Russian localization by @Ar2t1e ([#3](https://github.com/Sumire-Labs/Celeritas-Extra/pull/3)), their first contribution.
- Access Transformer configuration and support.

### Changed

- Enabled the LWJGL2 compatibility layer (`lwjglx`).
- Updated Gradle to 9.2.0, Shadow to 9.2.2, Unimined to 1.4.7-kappa, and the Foojay toolchain resolver to 1.0.0.
- Updated Actions setup-java, setup-gradle, and checkout to 5.0.0.

### Fixed

- Fixed `modRuntimeOnly` using the CleanroomModTemplate fix.

### Removed

- Removed the obsolete template `Mixin.json` file.

## [0.1.4] - 2025-10-28

### Added

- Chinese localization by @DHJComical ([#1](https://github.com/Sumire-Labs/Celeritas-Extra/pull/1)), their first contribution.

### Fixed

- Fixed the GitHub Actions build using the CleanroomModTemplate fix, contributed by @s12kuma01 ([#2](https://github.com/Sumire-Labs/Celeritas-Extra/pull/2)), their first pull request contribution.

## [0.1.3] - 2025-10-07

### Changed

- Updated Gradle from 9.0.0 to 9.1.0.

## [0.1.2] - 2025-10-07

### Removed

- Removed the cloud render distance feature.

## [0.1.1] - 2025-10-06

### Added

- Adjustable cloud render distance.

### Fixed

- Fixed the cloud height control not working.

## [0.1.0] - 2025-10-04

### Added

- Initial public release of Celeritas Extra for Minecraft 1.12.2 with Cleanroom Loader and Celeritas.
- Animation and particle toggles, graphics and rendering options, and quality-of-life features backported from Embeddium/Rubidium Extra.
