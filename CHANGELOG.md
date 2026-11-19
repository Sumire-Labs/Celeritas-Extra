# CHANGELOG

All notable changes to Celeritas Extra are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Release dates use Japan Standard Time (UTC+09:00). Entries are based on the
[published GitHub release notes](https://github.com/Sumire-Labs/Celeritas-Extra/releases)
and tagged commit history.

## [Unreleased]

### Changed

- Split sky/cloud settings from weather/fog, added a dedicated HUD page with FPS, coordinates, memory, time, world information, appearance and F3 sections, and grouped interface settings separately. HUD colors, dimension fog and per-mod particle controls now fold without discarding edits; both search engines include hidden settings and section names.
- Aligned translated labels and descriptions with upstream projects across English, Japanese, Russian and Simplified Chinese, while retaining accurate 1.12.2-specific explanations and localized text where upstream translations are incomplete.
- Enabled the experimental static item vertex cache by default and set the default item-frame LOD distance to 3 blocks. Existing saved choices are preserved.
- Ported the new Reese's Sodium Options GUI to Celeritas, including its wider layout, mod icon/version headers, tab styling, scroll bars and tooltips. Search results stay in their original mod pages; Enter/Shift+Enter or Tab/Shift+Tab navigate ranked results. Add-on controls, shader-pack actions, search-engine selection and per-option Undo/Reset settings remain supported.

## [0.11.1] - 2026-11-17

### Added

- Added separate settings for showing per-option Undo and Reset to Default buttons, both enabled by default. These settings work with either search engine and in the Pintonium and standalone Extra screens. Screen-wide Apply/Undo and Reset controls remain available.

### Fixed

- Fixed overlapping search bars with newer Celeritas builds. Added "Replace search engine with RSO", enabled by default, to switch between Extra's search and Celeritas's built-in search after Apply. Per-option Undo/Reset remains available in both modes.

## [0.11.0] - 2026-11-16

### Added

- Added per-option Undo and Reset to Default buttons with 11px image icons. Changes stay pending until Apply or Done. Reset supports Extra, Celeritas, vanilla-backed settings, and add-ons that supply defaults; other add-on options retain Undo. The standalone Extra screen also supports list settings.
- Ported the search engine from Reese's Sodium Options 2.2.5. Search tolerates typos, ranks results by relevance, and shows up to 15 results using the upstream thresholds. It preserves native action tabs, reuses its index across queries, and works in the standalone Extra screen.
- Integrated Extra settings into Pintonium's older video GUI, including its search and Apply/Undo controls. Per-option Undo/Reset buttons are available, and shader controls remain accessible. Newer Pintonium builds keep the Celeritas GUI integration.
- Added rain and snow frustum culling, enabled by default. It skips off-screen precipitation columns before generating vertices, including above the build limit. Custom weather renderers and shadow passes keep their normal rendering.
- Added optional HUD lines for local time (12/24-hour), world age, session duration excluding pauses, loaded/rendered entity counts, and biome. Each line has RGB controls, and memory usage has an optional details display. Clock, biome, and heap text are cached between updates.
- Added experimental MoreCulling-style three-face culling for blocks in item frames. New configurations enable it at 4 blocks, adjustable from 2 to 16 blocks. It requires an unrotated model and quarter-turn frame rotation. Block models within 3 blocks and shadow passes keep normal rendering, and Forge perspective transforms are checked before culling.

### Fixed

- Preserved integer slider ranges when saving settings so standalone sliders retain their values and bounds.

## [0.10.3] - 2026-11-14

### Changed

- Updated DevEnv.
- Camera-based optimizations stay disabled when the Iris shadow-pass state cannot be determined, preventing incorrect rendering during shadow passes.

### Removed

- Removed Actinium support. Actinium supports Cleanroom 0.6.x; Celeritas Extra requires 0.7.x. Celeritas and Nothirium remain supported.

### Fixed

- Fixed background FPS limits below 20 falling short of the selected rate. For example, a 15 FPS limit could produce about 10 FPS. The scheduler now carries timing remainders forward and avoids catch-up bursts after stalls.
- Fixed a crash when the block-entity dispatcher receives a null tile. Profiling also respects the dispatcher's invalid-tile check.
- Fixed Shift+Tab starting at the wrong field when no field is focused in the standalone list editor. It now selects the last field.
- Fixed VSync showing as enabled when the vanilla VSync setting is off and the saved adaptive-sync preference is on.
- Moved both item-frame LOD and vertex-cache invalidation to the render thread when models are rebaked, preventing concurrent cache access during asynchronous model baking.

## [0.10.2] - 2026-11-07

### Changed

- The experimental static item vertex cache now draws supported Forge item segments directly from cached GPU buffers, with reusable vertex array state. This avoids regenerating or re-uploading vertices on GPU cache hits while preserving Forge lighting and item-frame LOD.
- The cache remains off by default. Unsupported render paths retain the existing CPU cache or normal rendering, and the cache is bypassed with Iris or a renderer's native item cache.

### Fixed

- Lowered the optional AssetMover requirement to 2.5 so Modern Clouds works with the version available on CurseForge.

## [0.10.1] - 2026-11-03

### Requirements

- Requires Cleanroom Loader 0.7.14-alpha or newer within the 0.7.x series, and Java 25. The optional AssetMover integration now requires AssetMover 3.0.1 or newer.

### Changed

- Updated DevEnv.

## [0.10.0] - 2026-10-23

### Requirements

- Requires Cleanroom Loader 0.7.11-alpha or newer within the 0.7.x series, and Java 25. Cleanroom 0.6.x and 0.7.0 are no longer supported.
- Install exactly one supported renderer: Celeritas, Actinium, or Nothirium. The renderer must also support the installed Cleanroom version.

### Added

- Added an experimental static item vertex cache, disabled by default. It reuses vertex output for supported static item geometry while retaining Forge lighting and item-frame LOD. Performance gains vary by system and scene.
- Bounded the experimental cache to 256 entries and 4 MiB of vertex data. Dynamic geometry, registered item color handlers, Iris, and detected native renderer item caches use the normal rendering path.

### Changed

- Updated DevEnv.
- Use Cleanroom's SDL APIs directly for window controls and IME integration.
- Cache sprite animation categories, unchanged HUD text, and render-distance exemption checks.
- Reuse frame-statistics buffers and select the slowest percentile without sorting the full sample history.
- Avoid profiler name lookups when profiling is disabled, and close profiling scopes reliably.
- Calculate sign-text visibility once per render and cache filtered static item-frame LOD geometry.
- Cache particle policies and skip constructing known vanilla particles when their configured spawn rate is zero. Custom particle factories retain the normal discovery path.
- Clear item geometry caches when models are reloaded.

### Removed

- Removed the Cleanroom 0.6.x GLFW compatibility backend and reflective SDL compatibility helpers.
- Removed migration of the legacy combined `sunMoon` setting. Existing separate `sun` and `moon` settings remain supported; the obsolete key is removed when loading the configuration.

### Fixed

- Preserve vertex-buffer position and limit when capturing experimental item-cache entries, preventing corrupted item rendering.

## [0.9.3]

### Added

- Added a Cleanroom 0.7.0 SDL window backend while retaining the 0.6.x GLFW backend.

### Fixed

- Removed direct GLFW dependencies from screen-mode and VSync controls on Cleanroom 0.7.0.
- Read borderless and minimized states from SDL instead of removed or stubbed legacy Display methods.
- Apply adaptive VSync through SDL's negative swap interval, falling back to standard VSync on unsupported drivers.
- Restore the previous swap interval after checking adaptive VSync support.
- Connected Extra's custom search field to SDL's IME activation, composition and caret layout hooks.

## [0.9.2]

### Changed

- Updated DevEnv.

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
- Updated DevEnv.

### Fixed

- Fixed missing per-class controls, especially for modded particles.
- Corrected particle mod attribution instead of grouping all particles under "unknown".
- Prevented particle-page construction failures from breaking the rest of the settings screen.

## [0.4.6] - 2026-07-01

### Removed

- Removed the `COCOA_RETINA_FRAMEBUFFER` option.

## [0.4.5] - 2026-04-11

### Fixed

- Fixed a crash on devices that do not support adaptive VSync ([#12](https://github.com/Sumire-Labs/Celeritas-Extra/issues/12)).

## [0.4.4] - 2026-04-09

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

- Particle display names use the `ParticleFlame (minecraft)` format.
- Updated DevEnv.

### Requirements

- Requires Java 25 and Cleanroom Loader 0.5.6-alpha.

### Fixed

- Localized Screen Mode, Overlay Corner, Text Contrast, and VSync selector values.
- Removed literal newline escape sequences from tooltips in all four languages.

## [0.3.2] - 2026-03-24

### Added

- Adaptive VSync support with Off, On, and Adaptive modes, ported from Sodium Extra.
- GLX/WGL swap-control-tear detection and fallback on unsupported drivers.

### Changed

- Updated DevEnv.

## [0.3.1] - 2026-03-14

### Fixed

- Fixed a crash with LoliASM by moving `ProfilerHelper` out of the Mixin package.

## [0.3.0] - 2026-03-14

### Added

- Runtime class-based particle discovery and per-class toggles grouped by mod, replacing the static particle-ID filter.

### Changed

- Updated DevEnv.

### Fixed

- Fixed the screen turning white when fog was disabled.

## [0.2.4] - 2026-02-02

No user-facing changes.

## [0.2.3] - 2026-01-31

### Changed

- Updated Cleanroom Loader to 0.3.34.

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
- Adjusted the mod icon size.

## [0.1.14] - 2026-01-05

### Fixed

- Fixed a crash with the latest Celeritas version.

## [0.1.13] - 2026-01-02

### Removed

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

- Updated DevEnv.

## [0.1.7] - 2025-12-03

### Changed

- Updated DevEnv.

## [0.1.6] - 2025-11-23

### Added

- Thai localization (`th_th.lang`).

## [0.1.5] - 2025-11-07

### Added

- Russian localization by @Ar2t1e ([#3](https://github.com/Sumire-Labs/Celeritas-Extra/pull/3)), their first contribution.

### Changed

- Enabled the LWJGL2 compatibility layer (`lwjglx`).
- Updated DevEnv.

## [0.1.4] - 2025-10-28

### Added

- Chinese localization by @DHJComical ([#1](https://github.com/Sumire-Labs/Celeritas-Extra/pull/1)), their first contribution.

## [0.1.3] - 2025-10-07

### Changed

- Updated DevEnv.

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

[Unreleased]: https://github.com/Sumire-Labs/Celeritas-Extra/compare/0.11.1-release...HEAD
[0.11.1]: https://github.com/Sumire-Labs/Celeritas-Extra/compare/0.11.0-release...0.11.1-release
[0.11.0]: https://github.com/Sumire-Labs/Celeritas-Extra/compare/0.10.3-release...0.11.0-release
[0.10.3]: https://github.com/Sumire-Labs/Celeritas-Extra/compare/0.10.2-release...0.10.3-release
[0.10.2]: https://github.com/Sumire-Labs/Celeritas-Extra/releases/tag/0.10.2-release
