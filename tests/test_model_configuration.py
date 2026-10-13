"""Exercise model switching, secret boundaries and real local HTTP routing."""
import io
import json
import tempfile
import threading
import time
import unittest
import urllib.error
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from unittest.mock import patch

import uvicorn

from engine import MODEL_OPENER, ModelClient, ModelError, model_for_request, public_model_configuration, respond
from engine import test_model_connection as check_model_connection
from sahay.api.app import create_app

ENV = {'LLM_API_URL': 'https://provider.test/v1/chat/completions',
       'LLM_MODEL': 'default', 'LLM_API_KEY': 'test-backend-secret',
       'SAHAY_MODEL_ALLOWLIST': 'localhost:11434,other.test'}

def wire(text):
    return io.BytesIO(json.dumps({'choices': [{'message': {'content': text}}]}).encode())

class ModelConfigurationTests(unittest.TestCase):
    def test_switch_is_request_scoped_and_does_not_forward_default_key(self):
        with patch.dict('os.environ', ENV, clear=True):
            a = model_for_request({'model_config': {'url': 'http://localhost:11434/v1/chat/completions', 'model': 'gemma3:12b'}})
            b = model_for_request({'model_config': {'url': ENV['LLM_API_URL'], 'model': 'different-model'}})
            self.assertEqual(a.model, 'gemma3:12b'); self.assertEqual(a.key, '')
            self.assertEqual(b.key, ENV['LLM_API_KEY'])
            self.assertEqual(model_for_request({}).model, 'default')
            self.assertEqual(a.model, 'gemma3:12b')

    def test_endpoint_outside_the_allow_list_is_rejected(self):
        """X1: callers must not be able to aim the server at arbitrary or internal hosts."""
        with patch.dict('os.environ', ENV, clear=True), patch.object(MODEL_OPENER, 'open') as send:
            for url in ['http://169.254.169.254/latest/meta-data', 'http://127.0.0.1:8000/api/admin/users',
                        'http://localhost:9999/chat', 'https://evil.test/chat', 'http://10.0.0.5/v1/chat/completions']:
                with self.subTest(url=url), self.assertRaises(ValueError):
                    model_for_request({'model_config': {'url': url, 'model': 'm'}})
            send.assert_not_called()

    def test_without_an_allow_list_only_the_configured_endpoint_is_selectable(self):
        env = {k: v for k, v in ENV.items() if k != 'SAHAY_MODEL_ALLOWLIST'}
        with patch.dict('os.environ', env, clear=True):
            self.assertEqual(model_for_request({'model_config': {'url': ENV['LLM_API_URL'], 'model': 'x'}}).model, 'x')
            with self.assertRaises(ValueError):
                model_for_request({'model_config': {'url': 'http://localhost:11434/v1/chat/completions', 'model': 'x'}})
        with patch.dict('os.environ', {}, clear=True), self.assertRaises(ValueError):
            model_for_request({'model_config': {'url': 'https://provider.test/chat', 'model': 'x'}})

    def test_invalid_configuration_is_rejected_before_network_calls(self):
        bad = [None, 'file:///etc/passwd', 'https://user:secret@provider.test/chat',
               'https://provider.test/chat?key=secret', 'https://provider.test/chat#frag',
               'https://provider.test:0/chat', 'https://provider.test:bad/chat']
        for url in bad:
            with self.subTest(url=url), self.assertRaises(ValueError):
                model_for_request({'model_config': {'url': url, 'model': 'm'}})
        for value in ['x', {}, {'url': ENV['LLM_API_URL'], 'model': ''}]:
            with self.assertRaises(ValueError): model_for_request({'model_config': value})

    def test_connection_test_uses_selected_endpoint_and_actual_reply(self):
        with patch.dict('os.environ', ENV, clear=True), patch.object(MODEL_OPENER, 'open', return_value=wire('ready')) as send:
            result = check_model_connection({'model_config': {'url': 'https://other.test/chat', 'model': 'chosen'}})
            request = send.call_args.args[0]
            self.assertTrue(result['ok']); self.assertEqual(request.full_url, 'https://other.test/chat')
            self.assertEqual(json.loads(request.data)['model'], 'chosen')
            self.assertIsNone(request.get_header('Authorization'))
            self.assertEqual(ModelClient().model, 'default')

    def test_provider_restriction_is_visible_without_echoing_sensitive_body(self):
        payload = {'error': {'type': 'FreeTierError', 'message': 'test-backend-secret echoed text'}}
        error = urllib.error.HTTPError(ENV['LLM_API_URL'], 403, 'Forbidden', {}, io.BytesIO(json.dumps(payload).encode()))
        with patch.dict('os.environ', ENV, clear=True), patch.object(MODEL_OPENER, 'open', side_effect=error):
            result = check_model_connection({})
            self.assertFalse(result['ok']); self.assertIn('HTTP 403', result['error'])
            self.assertIn('restricted', result['error']); self.assertNotIn('test-backend-secret', str(result))

    def test_public_config_redacts_url_credentials(self):
        with patch.dict('os.environ', {**ENV, 'LLM_API_URL': 'https://u:secret@provider.test/chat?key=secret#secret'}, clear=True):
            self.assertEqual(public_model_configuration(), {'url': 'https://provider.test/chat', 'model': 'default'})

    def test_failure_reaches_chat_payload_and_urgent_still_skips_provider(self):
        with patch.dict('os.environ', ENV, clear=True), patch.object(MODEL_OPENER, 'open', side_effect=OSError('offline')) as send:
            result = respond({'text': 'I am worried about tomorrow'})
            self.assertIn('provider_error', result); self.assertNotEqual(result['mode'], 'live')
            send.reset_mock()
            result = respond({'text': 'I have a fever', 'model_config': {'url': ENV['LLM_API_URL'], 'model': 'chosen'}})
            self.assertEqual(result['route'], 'urgent_medical'); send.assert_not_called()

class LocalProvider(BaseHTTPRequestHandler):
    calls = []
    def log_message(self, *args): pass
    def do_POST(self):
        data = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
        type(self).calls.append((self.path, data['model'], self.headers.get('Authorization')))
        if self.path == '/redirect':
            self.send_response(302); self.send_header('Location', '/leak'); self.end_headers(); return
        system = data['messages'][0]['content']
        if 'classify risk' in system: text = json.dumps({'english': 'I feel worried', 'risk': 'ordinary'})
        elif 'Supervisor agent' in system: text = json.dumps({'action': 'reflect'})
        elif 'Review agent' in system: text = json.dumps({'pass': True})
        elif 'safety reviewer' in system: text = json.dumps({'verdict': 'allow', 'categories': []})
        else: text = 'That sounds difficult. What is worrying you today?'
        encoded = json.dumps({'choices': [{'message': {'content': text}}]}).encode()
        self.send_response(200); self.send_header('Content-Length', str(len(encoded))); self.end_headers(); self.wfile.write(encoded)

class ModelHttpTests(unittest.TestCase):
    def setUp(self):
        LocalProvider.calls = []
        self.provider = ThreadingHTTPServer(('127.0.0.1', 0), LocalProvider)
        threading.Thread(target=self.provider.serve_forever, daemon=True).start()
        self.url = f'http://127.0.0.1:{self.provider.server_port}/chat'
        self.tmp = tempfile.TemporaryDirectory()
        self.env = patch.dict('os.environ', {'LLM_API_URL': self.url, 'LLM_MODEL': 'default', 'LLM_API_KEY': 'secret',
                                             'SAHAY_DB_PATH': str(Path(self.tmp.name) / 'm.sqlite3')}, clear=True)
        self.env.start()
        self.api = uvicorn.Server(uvicorn.Config(create_app(), host='127.0.0.1', port=0, log_level='warning'))
        threading.Thread(target=self.api.run, daemon=True).start()
        while not self.api.started: time.sleep(0.02)
        self.api_port = self.api.servers[0].sockets[0].getsockname()[1]
    def tearDown(self):
        self.api.should_exit = True; time.sleep(0.2)
        self.env.stop(); self.tmp.cleanup()
        self.provider.shutdown(); self.provider.server_close()
        from sahay import db; db.dispose_all()
    def post(self, path, payload):
        request = urllib.request.Request(f'http://127.0.0.1:{self.api_port}'+path, json.dumps(payload).encode(), {'Content-Type': 'application/json'})
        with urllib.request.urlopen(request, timeout=5) as response: return json.load(response)
    def test_http_switch_reaches_every_agent_and_preserves_backend_default(self):
        config = {'url': self.url, 'model': 'device-model'}
        self.assertTrue(self.post('/api/model/test', {'model_config': config})['ok'])
        LocalProvider.calls.clear()
        result = self.post('/api/message', {'text': 'I feel worried', 'model_config': config})
        self.assertEqual(result['mode'], 'live')
        self.assertEqual([x['agent'] for x in result['agent_trace']], ['Language','Safety','Supervisor','Emotional support','Guard','Review'])
        self.assertEqual(len(LocalProvider.calls), 5)
        self.assertTrue(all(model == 'device-model' for _, model, _ in LocalProvider.calls))
        self.assertTrue(all(key == 'Bearer secret' for _, _, key in LocalProvider.calls))
        self.post('/api/model/test', {})
        self.assertEqual(LocalProvider.calls[-1][1], 'default')
    def test_redirect_does_not_forward_key_or_repeat_request(self):
        client = ModelClient(url=self.url.replace('/chat','/redirect'), key='secret', model='m')
        with self.assertRaises(ModelError): client.chat('system', 'hello')
        self.assertEqual(len(LocalProvider.calls), 1)
        self.assertEqual(LocalProvider.calls[0][0], '/redirect')
    def test_invalid_switch_returns_http_400(self):
        with self.assertRaises(urllib.error.HTTPError) as error:
            self.post('/api/model/test', {'model_config': {'url': 'not-url', 'model': 'm'}})
        self.assertEqual(error.exception.code, 400)
        error.exception.close()
        self.assertEqual(LocalProvider.calls, [])

if __name__ == '__main__': unittest.main()
