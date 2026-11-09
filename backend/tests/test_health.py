from fastapi.testclient import TestClient

from maale_bible_api.main import app


def test_liveness_reports_process_only() -> None:
    with TestClient(app) as client:
        response = client.get("/health/live")

    assert response.status_code == 200
    assert response.json() == {
        "status": "alive",
        "service": "maale-bible-api",
    }
    assert response.headers["cache-control"] == "no-store"
