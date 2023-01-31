import sys
from ch.systemsx.cisd.etlserver.registrator import JythonHookTestTool

jythonHookTestTool = JythonHookTestTool.createFromIncoming(incoming)

def did_encounter_secondary_transaction_errors(service, transaction, errors):
    jythonHookTestTool.log("did_encounter_secondary_transaction_errors %s" % errors.size())

# Execute a query
tr = service.transaction()
query = tr.getDatabaseQuery("path-info-db")
result = query.select("SELECT * from data_set_files WHERE parent_id is NULL", None)
sys.stdout.write("Roots: " + str(result.size()) + "\n")