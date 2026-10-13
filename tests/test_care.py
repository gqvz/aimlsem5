"""Participant isolation, unsolicited uploads and requested-file fulfillment."""
import base64
import tempfile
import unittest
from pathlib import Path
import bookkeeping as b
import care


class CareTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.old = b.DB_PATH
        b.DB_PATH = Path(self.temp.name) / 'care.sqlite3'
        b.init_db(); care.init_care()
        self.ids = {}; self.tokens = {}
        for name, role in [('patient','patient'), ('doctor','clinician'), ('outsider','patient')]:
            with b.connection() as db:
                self.ids[name] = b._create_user(db, name, name+'@example.test', 'StrongPass123', role)['id']
            _, login = b.handle('POST','/api/auth/login', {'contact':name+'@example.test','password':'StrongPass123'}, '')
            self.tokens[name] = 'Bearer '+login['token']
        _, result = self.call('POST','/api/care/threads', {'partner_id':self.ids['doctor']})
        self.thread = result['thread']['id']
        self.path = '/api/care/threads/'+self.thread+'/messages'

    def tearDown(self):
        b.DB_PATH = self.old; self.temp.cleanup()

    def call(self, method, path, data=None, who='patient'):
        return care.handle(method, path, data or {}, self.tokens[who])

    def file(self, text=b'Fictional course document', mime='text/plain', name='demo.txt'):
        return {'name':name,'mime':mime,'base64':base64.b64encode(text).decode()}

    def test_either_side_can_initiate_and_upload_without_request(self):
        _, same = self.call('POST','/api/care/threads', {'partner_id':self.ids['patient']}, 'doctor')
        self.assertEqual(same['thread']['id'], self.thread)
        for who in ('patient','doctor'):
            self.call('POST', self.path, {'files':[self.file(who.encode())]}, who)
        _, result = self.call('GET', self.path, who='doctor')
        self.assertEqual(len(result['messages']),2)
        for row, who in zip(result['messages'], ('patient','doctor')):
            content,mime,name = care.attachment(row['files'][0]['id'], self.tokens['patient'])
            self.assertEqual(content, who.encode())
            self.assertNotIn('base64',row['files'][0])

    def test_request_requires_file_and_fulfills_only_once(self):
        _, result = self.call('POST', self.path, {'kind':'file_request','text':'Please attach the fictional report'}, 'doctor')
        request = result['message_id']
        with self.assertRaises(b.ApiError): self.call('POST',self.path,{'text':'here','request_id':request})
        self.call('POST',self.path,{'request_id':request,'files':[self.file()]})
        _, rows = self.call('GET',self.path)
        self.assertIsNotNone(rows['messages'][0]['fulfilled_at'])
        with self.assertRaises(b.ApiError): self.call('POST',self.path,{'request_id':request,'files':[self.file()]})

    def test_outsider_cannot_read_write_or_download(self):
        self.call('POST',self.path,{'files':[self.file()]})
        _, rows = self.call('GET',self.path)
        file_id = rows['messages'][0]['files'][0]['id']
        for method,data in [('GET',{}),('POST',{'text':'intrusion'})]:
            with self.assertRaises(b.ApiError) as error: self.call(method,self.path,data,'outsider')
            self.assertEqual(error.exception.status,404)
        with self.assertRaises(b.ApiError): care.attachment(file_id,self.tokens['outsider'])
        with self.assertRaises(b.ApiError): care.attachment(file_id,'')

    def test_invalid_files_do_not_create_message_or_fulfill_request(self):
        _, result = self.call('POST',self.path,{'kind':'file_request','text':'Report please'},'doctor')
        for file in [self.file(b'not PDF','application/pdf'), self.file(b'x'*(care.MAX_FILE+1)), self.file(name='..'), self.file(mime='application/x-executable')]:
            with self.assertRaises(b.ApiError): self.call('POST',self.path,{'files':[file],'request_id':result['message_id']})
        _, rows = self.call('GET',self.path)
        self.assertEqual(len(rows['messages']),1)
        self.assertIsNone(rows['messages'][0]['fulfilled_at'])

    def test_inactive_partner_and_patient_request_are_rejected(self):
        with self.assertRaises(b.ApiError) as error: self.call('POST',self.path,{'kind':'file_request','text':'Report'})
        self.assertEqual(error.exception.status,403)
        with b.connection() as db: db.execute('UPDATE users SET active=0 WHERE id=?',(self.ids['doctor'],))
        with self.assertRaises(b.ApiError): self.call('GET',self.path)

