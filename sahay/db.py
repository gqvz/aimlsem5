"""Engine and session handling. The database file is resolved lazily so tests can repoint it."""
from __future__ import annotations

import threading
from collections.abc import Iterator
from contextlib import contextmanager
from pathlib import Path

from sqlalchemy import Engine, create_engine, event, inspect
from sqlalchemy.orm import Session, sessionmaker

from .config import Settings
from .models import Base

_lock = threading.Lock()
_engines: dict[Path, Engine] = {}


def _path() -> Path:
    return Settings.load().db_path


def get_engine(path: Path | None = None) -> Engine:
    path = path or _path()
    with _lock:
        engine = _engines.get(path)
        if engine is None:
            path.parent.mkdir(parents=True, exist_ok=True)
            engine = create_engine(f"sqlite:///{path}", connect_args={"timeout": 10, "check_same_thread": False})

            @event.listens_for(engine, "connect")
            def _pragmas(dbapi_connection, _record):
                cursor = dbapi_connection.cursor()
                cursor.execute("PRAGMA foreign_keys=ON")
                cursor.execute("PRAGMA journal_mode=WAL")
                cursor.close()

            _engines[path] = engine
        return engine


def dispose_all() -> None:
    with _lock:
        for engine in _engines.values():
            engine.dispose()
        _engines.clear()


@contextmanager
def session_scope() -> Iterator[Session]:
    session = sessionmaker(get_engine(), expire_on_commit=False)()
    try:
        yield session
        session.commit()
    except Exception:
        session.rollback()
        raise
    finally:
        session.close()


def get_session() -> Iterator[Session]:
    """FastAPI dependency: one transaction per request, rolled back on any error."""
    with session_scope() as session:
        yield session


def init_db() -> None:
    """Create or adopt the schema. Existing databases are stamped, never altered."""
    from alembic.config import Config

    from alembic import command

    engine = get_engine()
    cfg = Config(str(Path(__file__).resolve().parent.parent / "alembic.ini"))
    cfg.set_main_option("script_location", str(Path(__file__).resolve().parent.parent / "alembic"))
    cfg.attributes["engine"] = engine
    tables = set(inspect(engine).get_table_names())
    if "users" in tables and "alembic_version" not in tables:
        Base.metadata.create_all(engine)  # fills in any table an older install lacked
        command.stamp(cfg, "head")
    else:
        command.upgrade(cfg, "head")
