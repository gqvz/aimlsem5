FROM python:3.12-slim
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1 SAHAY_HOST=0.0.0.0 SAHAY_PORT=8000 SAHAY_DB_PATH=/data/sahay.sqlite3
WORKDIR /app
RUN useradd --uid 10001 --create-home sahay && mkdir /data && chown sahay:sahay /data
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY --chown=sahay:sahay engine.py agents.py server.py alembic.ini ./
COPY --chown=sahay:sahay sahay ./sahay
COPY --chown=sahay:sahay alembic ./alembic
COPY --chown=sahay:sahay web ./web
USER sahay
EXPOSE 8000
HEALTHCHECK --interval=30s --timeout=3s CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:8000/api/health', timeout=2)"
CMD ["python", "server.py"]
