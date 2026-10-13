# Stitch account and sharing references

These four HTML files were downloaded through the project's Stitch MCP screen records on 29 September 2026:

| Stitch screen | File |
| --- | --- |
| Welcome & Role Entry | `9b3a2e501b6e4ee5ae93e7dc912854c8.html` |
| Patient Account & Security | `6adec3129fbd401b94be22a0a2dc9c5a.html` |
| Clinician Invited Sign-In | `17ea2dc4de4f4a759e81a7f5e4ac27cc.html` |
| Share Visit Brief & Consent | `191f37e26ece44c496fcebfd37ebc9d1.html` |

They are design references, not shipped web screens. The Kotlin/Compose implementation follows their layout and palette but uses accurate course-demo copy. The generated HTML contains unsupported claims about named hospitals, a clinical pilot, specific encryption, clinical compliance, and automatic emergency handling. Those claims must not be copied into the app or presentation. The clinician queue/detail and admin screens were implemented in Compose using the same visual language because Stitch did not supply them.
