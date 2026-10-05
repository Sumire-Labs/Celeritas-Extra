# Celeritas/Actinium Extra

Celeritas (Actinium) Extra is an unofficial client-side add-on for Celeritas or Actinium, built to run on Cleanroom Loader.
It provides a wide range of quality-of-life and optimization options, similar to OptiFine and Angelica.

## Requirements

### Required

- [Cleanroom Loader](https://download.cleanroommc.com/) 0.6.10-alpha or newer
- One of the following:
  - [Celeritas](https://github.com/kappa-maintainer/Celeritas-auto-build) 2.4.0-dev.4 or newer
  - [Actinium](https://www.curseforge.com/minecraft/mc-mods/actinium-1-12-2-optimization) 0.0.12-alpha or newer
  - ~~[Nothirium](https://www.curseforge.com/minecraft/mc-mods/nothirium)~~ (support planned) ????

### Optional

- [AssetMover](https://www.curseforge.com/minecraft/mc-mods/assetmover) 2.5 or newer

## Features

The following settings and features are added by Celeritas Extra. Individual particle and dimension options vary depending on the installed mods and the connected server.

### Animations

- Toggle all texture animations
- Toggle water animations
- Toggle lava animations
- Toggle fire animations
- Toggle Nether portal animations
- Toggle other block texture animations

### Particles

- Toggle all particles
- Toggle rain splash particles
- Toggle particles when blocks are broken
- Toggle particles while mining blocks
- Adjust spawn percentages for individual particle classes (0–100%; 0% disables spawning)
- Automatically discover vanilla and modded particle classes

### Sky, Weather, and Colors

- Toggle sky rendering
- Toggle star rendering
- Adjust the number of stars
- Toggle sun rendering
- Toggle moon rendering
- Toggle rain and snow rendering
- Toggle biome-specific colors
- Toggle biome-specific sky colors
- Toggle void fog

### Clouds

- Toggle cloud rendering
- Adjust cloud height and render distance
- Adjust cloud scale (0.25×–4.00×)
- Select cloud translucency (default, always translucent, or always opaque)
- Modern Clouds: use the Minecraft 1.21.6 cloud texture

### Fog

- Toggle fog
- Adjust where fog begins
- Adjust fog distance
- Enable or disable per-dimension overrides
- Toggle fog for individual dimensions
- Adjust where fog begins for individual dimensions
- Adjust fog distance for individual dimensions
- Dimension options for vanilla dimensions, registered mod dimensions, and dimensions on the current server
- Preserve blindness, underwater, and lava fog even when normal fog is disabled

### Entities and Item Frames

- Toggle item frame rendering
- Toggle armor stand rendering
- Toggle painting rendering
- Toggle player name tags
- Toggle item frame name tags
- Limit entity render distance (0 keeps normal behavior)
- Limit tile entity render distance (0 keeps normal behavior)
- Specify classes exempt from render distance limits in the configuration file, using fully qualified class names or `package.*`
- Item frame LOD (0 disables it)

### Block Rendering, Lighting, and Screen Effects

- Toggle piston extension and retraction animation rendering
- Toggle beacon beam rendering
- Limit beacon beam height to the world height
- Toggle enchanting table book rendering
- Toggle back-face culling for sign text
- Toggle light updates
- Prevent vanilla screen shaders from loading, such as special spectator-mode views

### HUD and Overlays

- Display FPS
- Display coordinates
- Option to display coordinates even when debug information is restricted
- Display memory usage percentage
- Select the overlay position (top left, top right, bottom left, or bottom right)
- Select overlay text contrast (plain, background, or shadow)

### Debugging, Tooltips, and Notifications

- Adjust the F3 debug screen refresh interval
- Display renderer names in the F3 profiler pie chart
- Display the source mod's name in item tooltips
- Toggle all toast notifications or individual notification types

### Window and FPS Controls

- Select the screen mode (windowed, borderless, or fullscreen)
- Select VSync (off, on, or adaptive; adaptive requires a supported GPU)
- Limit FPS while the window is unfocused
- Limit FPS while the window is minimized
- Limit FPS on menu screens

### Settings Screen and Controls

- Search settings
- Display the number of search results
- Focus the search field with `Ctrl+F`
- Clear the search with `Esc` or the clear button
- Hold `Shift` and scroll over a slider row to adjust its value by the configured step

## Credits

- FlashyReese — Creator of Sodium Extra and Reese's Sodium Options
- dima_dencep — Creator of Rubidium/Embeddium Extra
- Txni — Creator of Sodium Extras
- FxMorin — Creator of MoreCulling
- 1foxy2 — Maintainer of MoreCulling
- GTNHteam — Maintainers of Angelica
- mitchej123
- Embeddedt — Creator of Celeritas and Embeddium
- CleanroomMC — Creators of Cleanroom Loader, CleanroomModTemplate, and related tools
- Everyone who provided translation keys

## AI Usage

Some code and ideas in this project were implemented with assistance from large language models (LLMs).
