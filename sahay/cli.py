"""Operator commands: `python -m sahay.cli seed-demo | migrate | openapi`."""
from __future__ import annotations

import json
import secrets
import sys

from sqlalchemy import select

from . import db
from .models import User
from .services import accounts


def seed_demo() -> None:
    db.init_db()
    with db.session_scope() as session:
        for role, name, contact in (("admin", "Demo Admin", "admin@sahay.demo"),
                                    ("clinician", "Dr. Meera Rao", "clinician@sahay.demo")):
            if session.execute(select(User.id).where(User.contact == contact)).first():
                print(f"{role}: already exists ({contact})")
                continue
            password = secrets.token_urlsafe(14)
            accounts.create_user(session, name, contact, password, role)
            print(f"{role}: {contact} / {password}")
    print("These are fictional local demo accounts. Save the generated passwords before closing this terminal.")


def main(argv: list[str]) -> int:
    command = argv[0] if argv else ""
    if command == "seed-demo":
        seed_demo()
    elif command == "migrate":
        db.init_db()
        print("Database is at the latest revision.")
    elif command == "openapi":
        from .api.app import create_app
        print(json.dumps(create_app().openapi(), indent=2, sort_keys=True, ensure_ascii=False))
    else:
        print("Usage: python -m sahay.cli seed-demo | migrate | openapi", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
