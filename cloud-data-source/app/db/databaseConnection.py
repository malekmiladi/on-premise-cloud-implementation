import couchdb

couch = couchdb.Server("http://admin:admin@192.168.1.18:5984/")
try:
    db = couch.create("cloud-init-config")
except couchdb.http.PreconditionFailed:
    db = couch["cloud-init-config"]
