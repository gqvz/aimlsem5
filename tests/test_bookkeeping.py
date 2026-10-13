"""Consent, role isolation, and revocation checks for fictional demo accounts."""

import tempfile
import unittest
from pathlib import Path

import bookkeeping


class BookkeepingTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.old_path = bookkeeping.DB_PATH
        bookkeeping.DB_PATH = Path(self.temp.name) / "test.sqlite3"
        bookkeeping.init_db()
        with bookkeeping.connection() as db:
            self.clinician = bookkeeping._create_user(db, "Dr. Meera Rao", "meera@example.test", "StrongPass123", "clinician")["id"]
            self.other_clinician = bookkeeping._create_user(db, "Dr. Anita Roy", "anita@example.test", "StrongPass123", "clinician")["id"]

    def tearDown(self):
        bookkeeping.DB_PATH = self.old_path
        self.temp.cleanup()

    def call(self, method, path, data=None, token=""):
        return bookkeeping.handle(method, path, data or {}, f"Bearer {token}" if token else "")

    def test_chat_path_is_not_claimed_by_account_router(self):
        self.assertIsNone(self.call("POST", "/api/message", {"text": "hello"}))

    def test_patient_controls_share_and_clinician_cannot_read_after_revoke(self):
        _, registration = self.call("POST", "/api/auth/register", {"name": "Asha Demo", "contact": "asha@example.test", "password": "PatientPass123", "language": "hi"})
        patient_token = registration["token"]
        _, doctor_login = self.call("POST", "/api/auth/login", {"contact": "meera@example.test", "password": "StrongPass123"})
        doctor_token = doctor_login["token"]
        _, other_login = self.call("POST", "/api/auth/login", {"contact": "anita@example.test", "password": "StrongPass123"})
        other_token = other_login["token"]
        payload = {"clinician_id": self.clinician, "summary": "I want to discuss travel after my visit.", "questions": ["When should I plan the next visit?"], "language": "hi"}
        with self.assertRaises(bookkeeping.ApiError):
            self.call("POST", "/api/shares", payload, patient_token)
        _, created = self.call("POST", "/api/shares", {**payload, "consent": True}, patient_token)
        share_id = created["share"]["id"]
        _, queue = self.call("GET", "/api/clinician/queue", token=doctor_token)
        self.assertEqual([row["id"] for row in queue["shares"]], [share_id])
        self.assertNotIn("summary", queue["shares"][0])
        with self.assertRaises(bookkeeping.ApiError) as denied:
            self.call("GET", f"/api/clinician/shares/{share_id}", token=other_token)
        self.assertEqual(denied.exception.status, 404)
        _, reviewed = self.call("POST", f"/api/clinician/shares/{share_id}/review", {"status": "reviewed", "note": "Discuss at the next visit."}, doctor_token)
        self.assertEqual(reviewed["share"]["status"], "reviewed")
        self.call("DELETE", f"/api/shares/{share_id}", token=patient_token)
        _, queue_after = self.call("GET", "/api/clinician/queue", token=doctor_token)
        self.assertEqual(queue_after["shares"], [])
        with self.assertRaises(bookkeeping.ApiError):
            self.call("GET", f"/api/clinician/shares/{share_id}", token=doctor_token)

    def test_patient_role_cannot_access_admin_or_clinician_routes(self):
        _, registration = self.call("POST", "/api/auth/register", {"name": "Ravi Demo", "contact": "ravi@example.test", "password": "PatientPass123"})
        token = registration["token"]
        for path in ("/api/clinician/queue", "/api/admin/users", "/api/admin/audit"):
            with self.subTest(path=path), self.assertRaises(bookkeeping.ApiError) as denied:
                self.call("GET", path, token=token)
            self.assertEqual(denied.exception.status, 403)

    def test_profile_and_password_settings(self):
        _, login = self.call("POST", "/api/auth/login", {"contact": "meera@example.test", "password": "StrongPass123"})
        token = login["token"]
        _, profile = self.call("POST", "/api/me/profile", {"name": "Dr. Meera Sharma"}, token)
        self.assertEqual(profile["user"]["name"], "Dr. Meera Sharma")
        _, restored = self.call("GET", "/api/me", token=token)
        self.assertEqual(restored["user"]["name"], "Dr. Meera Sharma")
        with self.assertRaises(bookkeeping.ApiError) as wrong:
            self.call("POST", "/api/me/password", {"current_password": "wrong", "new_password": "NewStrongPass123"}, token)
        self.assertEqual(wrong.exception.status, 403)
        self.call("POST", "/api/me/password", {"current_password": "StrongPass123", "new_password": "NewStrongPass123"}, token)
        with self.assertRaises(bookkeeping.ApiError) as expired:
            self.call("GET", "/api/me", token=token)
        self.assertEqual(expired.exception.status, 401)
        _, new_login = self.call("POST", "/api/auth/login", {"contact": "meera@example.test", "password": "NewStrongPass123"})
        self.assertEqual(new_login["user"]["role"], "clinician")


if __name__ == "__main__":
    unittest.main()
