from ch.systemsx.cisd.etlserver.registrator import JythonHookTestTool

jythonHookTestTool = JythonHookTestTool.createInTest()

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    jythonHookTestTool.log("rollback_transaction")

def commit_transaction(service, transaction):
    jythonHookTestTool.log("commit_transaction")

def pre_metadata_registration(context):
    jythonHookTestTool.log("pre_metadata_registration")

def post_metadata_registration(context):
    jythonHookTestTool.log("post_metadata_registration")
