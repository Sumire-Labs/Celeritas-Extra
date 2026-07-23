<div align="center">

# Celeritas Extra

**Aiming to bring compelling video options to Celeritas 1.12.2 (CRL).**

![Minecraft](https://img.shields.io/badge/Minecraft-1.12.2-62B47A?style=flat-square)
![Loader](https://img.shields.io/badge/Loader-Cleanroom-5865F2?style=flat-square)
![Requires](https://img.shields.io/badge/Requires-Celeritas-E67E22?style=flat-square)
![License](https://img.shields.io/badge/License-LGPL--3.0-97CA00?style=flat-square)

</div>

---

Celeritas Extra is an unofficial mod that ports and integrates features inspired by various mods — Sodium Extra among them — along with a number of original features, into Celeritas 1.12.2 (CRL).
Once installed, it lets you tweak fine-grained settings such as animations, particles, details, and the HUD — OptiFine-style — directly and intuitively from the Celeritas video-settings screen.

## 📦 Requirements

| | |
|---|---|
| **Loader** | Cleanroom Loader 0.5.0+ |
| **Dependency** | [Celeritas](https://github.com/kappa-maintainer/Celeritas-auto-build/releases) — available from kappa-maintainer's auto-build repository |

## ✨ Features

### Animations
Master toggle for all animations, plus individual control of **water, lava, fire, portal, and block** animations.

### Particles
- Global particle toggle, with individual toggles for **rain splash, block break, and block-breaking** particles.
- **Per-particle-class control** — every particle type (including modded ones) is auto-discovered and can be toggled individually. Classes are found by scanning the registered particle factories at world load, supplemented by runtime detection as particles spawn (the only reliable way to catch mod particles registered as lambdas). Discovered classes are cached so they appear from launch in later sessions, and the cache is pruned when a mod is removed.

### Details
Toggle **sky, stars** (with an adjustable star count), **sun & moon, rain & snow, biome colors, sky colors, void particles, and void fog**.

### Render
- **Fog** — toggle, start-distance multiplier, and render distance (in chunks). Gameplay fog (blindness, underwater, lava) is always preserved, even with fog turned off.
- **Clouds** — toggle, height, render distance (extended range), scale, and translucency (Default / Always / Never).
- **Item Frame LOD** — beyond a configurable distance, framed items render with fewer faces and framed maps are hidden, to lighten large item-frame and map walls. *(Off by default.)*
- **Entity & block rendering toggles** — item frames, armor stands, paintings, pistons, beacon beams (with an optional beam-height limit), and enchanting-table books.
- **Name tags** — player and item-frame name-tag toggles.
- **Light Updates** toggle and **Prevent Shaders** (blocks vanilla screen shaders, e.g. the spider-vision distortion).

### Extras
- **FPS overlay** — current FPS, with optional extended metrics (average, 1% low, 0.1% low).
- **Coordinates overlay** — with corner position, text contrast (None / Background / Shadow), and an "ignore reduced debug info" option.
- **Mod Name Tooltip** — show the source mod's name at the bottom of every item's tooltip.
- **Toasts** — master toggle plus per-type control (advancement, recipe, tutorial, system).
- **Steady Debug HUD** — refresh the F3 screen on a fixed tick interval instead of every frame.
- **Reduced Motion**.
- **Hide HEI Until Searching** — keeps the HEI item list hidden until you type in the search bar *(only available when HEI is installed)*.

### Window
- **Screen Mode** — Windowed / Borderless / Fullscreen (replaces the vanilla fullscreen toggle).
- **VSync** — Off / On / Adaptive (replaces the vanilla VSync toggle).

## 📄 License
Licensed under **LGPL-3.0** — see [LICENSE.md](LICENSE.md).

## 🙏 Credits
- **FlashyReese** — creator of Sodium Extra.
- **embeddedt** — creator of Celeritas.
- **CleanroomMC** — CleanroomModTemplate and various other resources.
- Everyone who contributed translations.

## ⚠️ Notice
Some of this mod's code was generated with the help of Claude. All AI-assisted code is reviewed before it is included.
