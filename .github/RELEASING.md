# Publishing releases

Pushing a release tag runs `clean build verifyReleaseJar`, creates a GitHub
release and uploads the verified runtime JAR to CurseForge (`1451214`) and
Modrinth (`LXQekDcE`). Development and source JARs are not uploaded as mod files.
There is no Maven upload in this workflow.

Set these repository Actions secrets before pushing a tag:

| Secret | Value |
| --- | --- |
| `CURSEFORGE_TOKEN` | Upload token for an account with access to Celeritas Extra |
| `MODRINTH_TOKEN` | Personal access token with `CREATE_VERSION` and `VERSION_WRITE`, for an account with access to Celeritas Extra |

The workflow uses the version and release stage from Cleanroom Versioning.
Explicit `-alpha`, `-beta`, and `-release` tag suffixes select that channel;
`-rc` selects beta. Bare tags inherit the stage from the versioning plugin;
without a staged tag in the same major version, 0.x defaults to beta and 1.x+
defaults to release. Alpha and beta GitHub releases are marked as prereleases.
The tag must match the Gradle version after stripping the optional `v` prefix
and stage suffix. Add a matching `## [<version>]` section to `CHANGELOG.md` before tagging:
its contents are used as release notes on all three sites.

For manual publication, run **Releases** from the Actions tab on a branch with
this workflow. Enter an existing release tag, select the destinations, and leave
the channel at `auto` to derive it from the tag. An explicit channel can override
that choice. The selected tag must contain the build and verification tasks used
by the workflow. Checkouts always use the tag, even when the workflow is launched
from a branch.

If publication partially succeeds, run it manually with only the remaining
destinations enabled. Leave `publish_github` disabled if its release already
exists. Successful uploads are not rolled back automatically.

Validate without uploading:

```text
./gradlew build verifyReleaseJar
```

All site uploads use Minecraft 1.12.2, the Forge listing category, and Java 25.
The mod itself requires Cleanroom; the loader requirement and choice of supported
renderer remain in the mod's metadata and release notes. The workflow does not
add an unconditional dependency on any single renderer.
