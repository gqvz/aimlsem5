"""Provider contracts tested with mocked HTTP; no speech API provisioned yet."""
import base64
import io
import json
import unittest
from unittest.mock import patch

from sahay.errors import ApiError
from sahay.providers import speech


class SpeechTests(unittest.TestCase):
    def test_unconfigured_provider_is_explicit(self):
        with patch.dict('os.environ',{},clear=True):
            self.assertFalse(speech.status()['stt'])
            for call,body in [(speech.transcribe,{'base64':'YQ=='}),(speech.synthesize,{'text':'hello'})]:
                with self.assertRaises(ApiError) as error: call(body)
                self.assertEqual(error.exception.status,503)
    def test_stt_multipart_language_audio_and_provider_key(self):
        with patch.dict('os.environ',{'STT_API_URL':'https://provider.test/transcribe','STT_API_KEY':'test-secret'}), patch('urllib.request.urlopen',return_value=io.BytesIO(json.dumps({'text':'नमस्ते'}).encode())) as send:
            result=speech.transcribe({'language':'hi','base64':base64.b64encode(b'fake-audio').decode()})
            request=send.call_args.args[0]
            self.assertEqual(result['text'],'नमस्ते')
            self.assertIn(b'name="language"\r\n\r\nhi',request.data)
            self.assertIn(b'fake-audio',request.data)
            self.assertEqual(request.get_header('Authorization'),'Bearer test-secret')
    def test_tts_json_mp3_and_upstream_error(self):
        with patch.dict('os.environ',{'TTS_API_URL':'https://provider.test/speech'}), patch('urllib.request.urlopen',return_value=io.BytesIO(b'ID3fake')) as send:
            self.assertEqual(speech.synthesize({'text':'A gentle moment'}),b'ID3fake')
            self.assertEqual(json.loads(send.call_args.args[0].data)['response_format'],'mp3')
        with patch.dict('os.environ',{'TTS_API_URL':'https://provider.test/speech'}), patch('urllib.request.urlopen',side_effect=OSError):
            with self.assertRaises(ApiError) as error: speech.synthesize({'text':'hello'})
            self.assertEqual(error.exception.status,502)
    def test_invalid_audio_language_is_rejected(self):
        with patch.dict('os.environ',{'STT_API_URL':'https://provider.test/transcribe'}):
            with self.assertRaises(ApiError): speech.transcribe({'language':'unknown','base64':'YQ=='})
            with self.assertRaises(ApiError): speech.transcribe({'language':'en','base64':'invalid?'})
