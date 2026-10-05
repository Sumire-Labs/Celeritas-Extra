# 0.8.0

This release adds Actinium alpha-0.0.12 support alongside Celeritas. Install one
renderer with Cleanroom Loader and Java 25. Both integrations share the existing
`config/celeritas-extra.cfg`; upgrading does not require deleting your settings.

## Changes

- FPS overlay statistics now show average FPS, 1% low and 0.1% low; optional memory usage joins FPS and coordinates.
- Corrected frame LOD for sprite items, including Forge's emissive item path and Actinium's fast item renderer.
- Added sign-text and framed-map back-face culling, entity/TESR distance limits and per-particle spawn percentages.
- Added separate sun/moon controls, per-dimension fog overrides and menu/inactive/minimized FPS limits.
- Organized settings into ten pages with labeled groups and updated all four bundled languages.
- Celeritas's options screen gains search/filtering, Shift-wheel slider adjustment and collapsible mod and option groups.
- Actinium uses its integrated Reese's Sodium Options frontend, including its native search and folding. Native cloud and menu FPS controls take precedence over duplicate Extra controls.
- Camera-relative culling and fog overrides preserve Iris shadow passes.

## Release audit

- Fixed the search-selection caret position.
- Fixed FPS statistics when the monotonic clock starts with negative timestamps.
- Early configuration reads now use the launched game's directory; Forge's later canonical directory is respected.
- Release metadata supports either renderer, and development jars are excluded from release uploads.
- Automated tests cover native GUI controls, search/collapse behavior, pending values, backend API linkage, persistence and translations.
- `verifyReleaseJar` checks the actual remapped jar's metadata, registered Mixins/refmap, bundled languages and absence of renderer/game classes.

In-game GUI rendering and shader-pack appearance have not been verified in this
workspace. Shader packs can provide their own sky, clouds and fog behavior.
