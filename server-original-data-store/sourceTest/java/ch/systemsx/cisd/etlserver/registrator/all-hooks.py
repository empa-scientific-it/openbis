from ch.systemsx.cisd.etlserver.registrator import JythonHookTestTool

jythonHookTestTool = JythonHookTestTool.createInTest()

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    jythonHookTestTool.log("rollback_transaction")

def rollback_pre_registration(context, throwable):
    jythonHookTestTool.log("rollback_pre_registration")

def commit_transaction(service, transaction):
    jythonHookTestTool.log("commit_transaction")

def post_storage(context):
    jythonHookTestTool.log("post_storage")

def pre_metadata_registration(context):
    jythonHookTestTool.log("pre_metadata_registration")

def post_metadata_registration(context):
    jythonHookTestTool.log("post_metadata_registration")
