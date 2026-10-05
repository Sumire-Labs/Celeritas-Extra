# Actinium integration

Celeritas Extra supports Celeritas or Actinium in the same jar. Install exactly
one renderer. Both use `config/celeritas-extra.cfg` and the same option definitions.

The Actinium adapter targets **alpha-0.0.12** (source revision
`941a67d70fc9fa77650512fe2963142db151d3c5`). It links directly to Actinium's
`dhj.embeddedt.embeddium.api` option model and construction events. It does not
require Actinium to expose the old `org.taumc.celeritas.api` binary API.

`gradle/scripts/actinium.gradle` downloads the pinned release with a SHA-256 check
and generates Actinium option classes from the shared definitions. The dependency
is compile-only; no Actinium or Iris classes are bundled in Celeritas Extra.

Actinium supplies its own Reese's Sodium Options frontend and search. Extra's
Celeritas-specific frontend mixins are disabled in Actinium installations. Its
native fullscreen-mode control is preserved; the VSync option gains adaptive sync.

Frame LOD uses the ordinary Forge item path while LOD is active, avoiding the
cached geometry in Actinium's fast lit-item renderer. Other items retain that
optimization. Camera-relative entity/TESR distance limits, sign/map back-face
culling, frame LOD and fog overrides are bypassed during Iris shadow passes.

Shader packs may implement their own sky, clouds or fog. The corresponding Extra
options control Minecraft's rendering and cannot override arbitrary shader-pack
programs. GUI/API linkage and automated tests do not substitute for in-game checks
with the selected shader pack.
