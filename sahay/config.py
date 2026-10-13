"""Runtime configuration read from the environment (see .env.example)."""
from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


@dataclass(frozen=True)
class Settings:
    db_path: Path
    web_root: Path
    trust_proxy: bool
    session_hours: int = 12

    @staticmethod
    def load() -> Settings:
        return Settings(
            db_path=Path(os.getenv("SAHAY_DB_PATH", str(ROOT / "sahay_demo.sqlite3"))),
            web_root=ROOT / "web",
            trust_proxy=os.getenv("SAHAY_TRUST_PROXY") == "1",
        )
