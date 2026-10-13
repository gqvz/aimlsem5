"""Run the Sahay API and web client: `python server.py` (or `uvicorn sahay.api.app:app`)."""
import os

import uvicorn

if __name__ == "__main__":
    host = os.getenv("SAHAY_HOST", "127.0.0.1")
    port = int(os.getenv("SAHAY_PORT", "8000"))
    uvicorn.run("sahay.api.app:app", host=host, port=port, log_level="warning", access_log=False)
