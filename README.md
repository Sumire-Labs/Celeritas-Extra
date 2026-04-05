# Celeritas Extra

This is an unofficial backport of Sodium Extra for Celeritas 12.2 (Cleanroom Loader).
Additionally, several custom enhancements are being implemented.

For improved QoL, you may also want to check out these additional mods:
[CeleritasLeafCulling](https://github.com/Karnatour/CeleritasLeafCulling)
[CeleritasDynamicLights](https://github.com/Karnatour/CeleritasDynamicLights)

You can obtain Celeritas from this AutoBuild repository maintained by kappa-maintainer:
[Celeritas Auto Build](https://github.com/kappa-maintainer/Celeritas-auto-build/releases)

## Description

Celeritas Extra is a comprehensive unofficial backport of Sodium Extra/Embeddium Extra, specifically targeting Celeritas
1.12.2, along with several additional features.

## Features

### Animation Control
- Toggle all animations globally, or individually control water, lava, fire, portal, and block animations

### Particle Control
- Global particle toggle with individual controls for rain splash, block break, and block breaking particles
- Dynamic per-class particle control — automatically discovers all particle classes (including mod particles) via ASM scanning and allows toggling each one individually

### Detail Settings
- Toggle sky, stars, sun/moon, rain/snow rendering, and biome colors

### Render Settings
- Fog control (toggle, start distance, render distance, fog type)
- Cloud toggle and height adjustment
- Light update toggle
- Toggle rendering of item frames, armor stands, paintings, pistons, beacons, enchanting table books
- Beacon beam height limiting
- Player and item frame name tag toggles
- Shader prevention

### Extra Settings
- FPS overlay with 1% low and 0.1% low percentile metrics (industry-standard frame timing analysis)
- Coordinates overlay with configurable corner position and text contrast (None / Background / Shadow)
- Steady Debug HUD with configurable refresh interval
- Reduced motion option
- Hide HEI item list until searching (optional, only available when HEI is installed)

### Window Settings (Celeritas GUI integration)
- Screen Mode selector (Windowed / Borderless / Fullscreen) — replaces vanilla fullscreen toggle
- Adaptive VSync (Off / On / Adaptive) — replaces vanilla VSync toggle
- macOS Retina Framebuffer toggle (grayed out on non-macOS)

### Profiler
- Entity and tile entity render profiler sections for performance analysis

## Requirements

* **CleanroomLoader**
* **Celeritas**

## License

LGPL-3.0 - For details, please refer to the LICENSE.md file.

## Credits

* **dima_dencep** - Creator of Embeddium Extra.
* **embeddedt** - Creator of Celeritas.
* **CleanroomMC** - This mod utilizes the Cleanroom Mod Template.
* All contributors who provided translation keys for their languages
