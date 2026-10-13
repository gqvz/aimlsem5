"""Baseline: the schema the prototype created with CREATE TABLE IF NOT EXISTS.

Revision ID: 0001
Revises:
"""
from alembic import op

from sahay.models import Base

revision = "0001"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    Base.metadata.create_all(op.get_bind())


def downgrade() -> None:
    Base.metadata.drop_all(op.get_bind())
