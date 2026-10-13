"""Authenticated two-way patient/clinician conversations and small demo files."""
import base64
import secrets
import sqlite3
from pathlib import Path
from bookkeeping import connection, _auth, _audit, _string, utc_now, ApiError

MAX_FILE = 5 * 1024 * 1024
ALLOWED = {'application/pdf', 'image/png', 'image/jpeg', 'text/plain'}

def init_care():
    with connection() as db:
        db.executescript('''
          CREATE TABLE IF NOT EXISTS care_threads (
            id TEXT PRIMARY KEY, patient_id TEXT NOT NULL REFERENCES users(id),
            clinician_id TEXT NOT NULL REFERENCES users(id), created_at TEXT NOT NULL,
            UNIQUE(patient_id,clinician_id));
          CREATE TABLE IF NOT EXISTS care_messages (
            id TEXT PRIMARY KEY, thread_id TEXT NOT NULL REFERENCES care_threads(id),
            sender_id TEXT NOT NULL REFERENCES users(id), text TEXT NOT NULL,
            kind TEXT NOT NULL, request_id TEXT, fulfilled_at TEXT, created_at TEXT NOT NULL);
          CREATE TABLE IF NOT EXISTS care_files (
            id TEXT PRIMARY KEY, message_id TEXT NOT NULL REFERENCES care_messages(id),
            name TEXT NOT NULL, mime TEXT NOT NULL, bytes BLOB NOT NULL, size INTEGER NOT NULL);
        ''')

def _thread(db, thread_id, user):
    row = db.execute('SELECT * FROM care_threads WHERE id=? AND (patient_id=? OR clinician_id=?)', (thread_id,user['id'],user['id'])).fetchone()
    if row is None: raise ApiError(404,'Conversation unavailable')
    partner = row['clinician_id'] if user['id']==row['patient_id'] else row['patient_id']
    if db.execute('SELECT 1 FROM users WHERE id=? AND active=1',(partner,)).fetchone() is None:
        raise ApiError(403,'This care-team account is inactive')
    return row

def _file_data(item):
    if not isinstance(item,dict): raise ApiError(400,'Invalid attachment')
    name=Path(_string(item.get('name'),'file name',120)).name
    if not name or name in ('.','..'): raise ApiError(400,'Invalid file name')
    mime=str(item.get('mime',''))
    if mime not in ALLOWED: raise ApiError(400,'Use PDF, PNG, JPEG or plain text files')
    try: payload=base64.b64decode(item.get('base64',''),validate=True)
    except (ValueError,TypeError): raise ApiError(400,'Invalid file encoding')
    if not payload or len(payload)>MAX_FILE: raise ApiError(413,'Each file must be between 1 byte and 5 MB')
    if mime=='application/pdf' and not payload.startswith(b'%PDF-'): raise ApiError(400,'This is not a PDF file')
    if mime=='image/png' and not payload.startswith(b'\x89PNG\r\n\x1a\n'): raise ApiError(400,'This is not a PNG file')
    if mime=='image/jpeg' and not payload.startswith(b'\xff\xd8\xff'): raise ApiError(400,'This is not a JPEG file')
    if mime=='text/plain':
        try: payload.decode('utf-8')
        except UnicodeDecodeError: raise ApiError(400,'Use UTF-8 text')
    return name,mime,payload

def handle(method,path,data,authorization):
    if not path.startswith('/api/care/'): return None
    with connection() as db:
        user=_auth(db,authorization,('patient','clinician'))
        if method=='GET' and path=='/api/care/contacts':
            role='clinician' if user['role']=='patient' else 'patient'
            rows=db.execute('SELECT id,name,role FROM users WHERE role=? AND active=1 ORDER BY name',(role,)).fetchall()
            return 200,{'contacts':[dict(r) for r in rows]}
        if method=='GET' and path=='/api/care/threads':
            rows=db.execute('''SELECT t.*,p.name patient_name,c.name clinician_name,
                (SELECT text FROM care_messages WHERE thread_id=t.id ORDER BY created_at DESC,rowid DESC LIMIT 1) preview,
                (SELECT count(*) FROM care_messages WHERE thread_id=t.id AND kind='file_request' AND fulfilled_at IS NULL) open_requests
                FROM care_threads t JOIN users p ON p.id=t.patient_id JOIN users c ON c.id=t.clinician_id
                WHERE t.patient_id=? OR t.clinician_id=? ORDER BY t.created_at DESC''',(user['id'],user['id'])).fetchall()
            return 200,{'threads':[dict(r) for r in rows]}
        if method=='POST' and path=='/api/care/threads':
            partner=_string(data.get('partner_id'),'care contact',40)
            opposite='clinician' if user['role']=='patient' else 'patient'
            if db.execute('SELECT 1 FROM users WHERE id=? AND role=? AND active=1',(partner,opposite)).fetchone() is None:
                raise ApiError(404,'Care contact unavailable')
            patient,doctor=(user['id'],partner) if user['role']=='patient' else (partner,user['id'])
            db.execute('INSERT OR IGNORE INTO care_threads VALUES(?,?,?,?)',(secrets.token_hex(10),patient,doctor,utc_now()))
            row=db.execute('SELECT * FROM care_threads WHERE patient_id=? AND clinician_id=?',(patient,doctor)).fetchone()
            _audit(db,user['id'],'open_conversation','care_thread',row['id'])
            return 201,{'thread':dict(row)}
        parts=path.strip('/').split('/')
        if len(parts)==5 and parts[:3]==['api','care','threads'] and parts[4]=='messages':
            thread_id=parts[3]; _thread(db,thread_id,user)
            if method=='GET':
                rows=db.execute('SELECT m.*,u.name sender_name,u.role sender_role FROM care_messages m JOIN users u ON u.id=m.sender_id WHERE thread_id=? ORDER BY m.created_at,m.rowid',(thread_id,)).fetchall()
                messages=[]
                for row in rows:
                    files=db.execute('SELECT id,name,mime,size FROM care_files WHERE message_id=?',(row['id'],)).fetchall()
                    messages.append({**dict(row),'files':[dict(f) for f in files]})
                return 200,{'messages':messages}
            if method=='POST':
                text=_string(data.get('text'),'message',2000,required=False)
                kind=data.get('kind','note')
                if kind not in ('note','file_request'): raise ApiError(400,'Invalid message kind')
                if kind=='file_request' and user['role']!='clinician': raise ApiError(403,'Only clinicians can request medical files')
                items=data.get('files',[])
                if not isinstance(items,list) or len(items)>2: raise ApiError(400,'Attach up to two files per message')
                files=[_file_data(f) for f in items]
                if not text and not files: raise ApiError(400,'Add a message or file')
                if kind=='file_request' and not text: raise ApiError(400,'Describe the file you are requesting')
                request_id=_string(data.get('request_id'),'request',40,required=False)
                if request_id:
                    request=db.execute("SELECT * FROM care_messages WHERE id=? AND thread_id=? AND kind='file_request' AND sender_id<>? AND fulfilled_at IS NULL",(request_id,thread_id,user['id'])).fetchone()
                    if request is None or not files: raise ApiError(400,'Choose an open request and attach a file')
                    db.execute('UPDATE care_messages SET fulfilled_at=? WHERE id=?',(utc_now(),request_id))
                message_id=secrets.token_hex(10)
                db.execute('INSERT INTO care_messages(id,thread_id,sender_id,text,kind,request_id,created_at) VALUES(?,?,?,?,?,?,?)',(message_id,thread_id,user['id'],text,kind,request_id,utc_now()))
                for name,mime,payload in files:
                    db.execute('INSERT INTO care_files VALUES(?,?,?,?,?,?)',(secrets.token_hex(10),message_id,name,mime,payload,len(payload)))
                _audit(db,user['id'],'send_'+kind,'care_thread',thread_id)
                return 201,{'ok':True,'message_id':message_id}
    raise ApiError(404,'Not found')

def attachment(file_id,authorization):
    with connection() as db:
        user=_auth(db,authorization,('patient','clinician'))
        file=db.execute('SELECT f.*,m.thread_id FROM care_files f JOIN care_messages m ON m.id=f.message_id WHERE f.id=?',(file_id,)).fetchone()
        if file is None: raise ApiError(404,'File unavailable')
        _thread(db,file['thread_id'],user)
        _audit(db,user['id'],'download_file','care_file',file_id)
        return bytes(file['bytes']),file['mime'],file['name']
