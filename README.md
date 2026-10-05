# Celeritas Extra

Celeritas Extra is an unofficial client-side add-on for [Celeritas](https://github.com/kappa-maintainer/Celeritas-auto-build/releases), built to run on Cleanroom. It adds more graphics, particle, HUD, and window settings to the Celeritas video settings screen.

The mod started as a port of features from Sodium Extra and Rubidium/Embeddium Extra. It also includes several additions and backports made specifically for the Cleanroom environment.

## Requirements

- [Cleanroom Loader](https://github.com/CleanroomMC/Cleanroom) 0.6.10-alpha or newer
- [Celeritas](https://github.com/kappa-maintainer/Celeritas-auto-build/releases) 2.4.0 or newer

### Optional dependency

- [AssetMover](https://github.com/CleanroomMC/AssetMover) 2.5 or newer unlocks Modern Clouds.

## Features

- Animation controls for water, lava, fire, portals, and block textures.
- Global and per-type particle controls, including options for rain splashes and block particles. Vanilla and modded particle classes are discovered automatically and cached between launches.
- Controls for the sky, stars and star count, the sun and moon independently, weather, biome colors, sky colors, and void fog. Existing combined sun/moon settings are migrated automatically.
- Fog distance and start controls. Blindness, underwater, and lava fog are kept even when normal fog is disabled.
- Cloud height, distance, scale (0.25x to 4.00x), and translucency controls, plus the optional Minecraft 1.21.6 cloud texture downloaded by AssetMover. Modern Clouds is disabled by default and uses a built-in resource pack with the existing renderer. User resource packs take priority; height, distance, and scale remain controlled by the existing Forge cloud settings.
- MoreCulling-style item frame LOD: beyond the configured distance, ordinary sprite items render without their edge faces and, where the fixed transform allows, without the hidden back face. Maps and 3D block items retain their normal rendering. It is disabled by default.
- Render toggles for item frames, armor stands, paintings, pistons, beacon beams, enchanting table books, name tags, light updates, and vanilla screen shaders.
- Back-face culling for sign text and framed maps. Sign editing and views near the sign plane remain visible. Both features can be disabled independently.
- FPS, coordinate, and JVM heap usage overlays with configurable position and text contrast. Current FPS is smoothed over 0.5 seconds; extended statistics include average FPS, 1% low, and 0.1% low over the last 5 seconds, refreshed every 0.5 seconds. Heap usage shows the percentage and used/max MiB.
- Mod-name tooltips, per-type toast controls, a steady F3 debug screen, and renderer names in the F3 profiler pie chart.
- Windowed, borderless, and fullscreen modes, plus Off, On, and Adaptive VSync.

## Credits

- FlashyReese, creator of Sodium Extra
- dima_dencep, creator of Rubidium and Embeddium Extra
- embeddedt, creator of Celeritas
- CleanroomMC, for Cleanroom Loader, CleanroomModTemplate, and related tools
- Everyone who has contributed translations

## License

Celeritas Extra is licensed under the [LGPL-3.0](LICENSE.md).

## AI usage

Some code in this project was written with AI assistance. Changes are reviewed before they are included.
