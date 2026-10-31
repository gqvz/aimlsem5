# Sahay architecture diagrams

Eight diagrams, each sized for one A4 landscape page. Print `sahay-architecture-a4.pdf` directly, at 100% scale ("Actual size"), or render the `.mmd` sources with any Mermaid 11 tool.

| Page | File | Covers | Printed text |
| --- | --- | --- | --- |
| 1 | `01-system-overview.mmd` | The three systems and their layers | 9.7 pt |
| 2 | `02-frontend-screens.mmd` | Role-based screens and on-device features | 11.8 pt |
| 3 | `03-frontend-state-security.mmd` | Application state and client security | 12.5 pt |
| 4 | `04-backend-request-access.mmd` | Request security and access control | 10.3 pt |
| 5 | `05-backend-services-data.mmd` | Domain services, SQLite and deployment | 8.3 pt |
| 6 | `06-ai-input-orchestration.mmd` | Input guardrails and orchestration | 11 pt |
| 7 | `07-ai-specialists-output.mmd` | Specialists, output guardrails and response | 8.9 pt |
| 8 | `08-ai-model-access.mmd` | Model access security and external providers | 12.3 pt |

## Colour key

| Colour | Meaning |
| --- | --- |
| Blue | Functional layer or component |
| Red | Security or safety control |
| Green | Data store |
| Dashed purple | External provider |
| Amber | Link to another page |

These diagrams show implemented controls only. Known security gaps and differences from `ARCHITECTURE.md` are not drawn.

The PDF does not update when a `.mmd` file changes, so regenerate it after editing a diagram.
