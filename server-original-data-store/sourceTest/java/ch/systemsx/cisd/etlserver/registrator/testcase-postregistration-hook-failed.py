from java.lang import IllegalArgumentException
from ch.systemsx.cisd.etlserver.registrator import JythonHookTestTool

jythonHookTestTool = JythonHookTestTool.createFromIncoming(incoming)

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def post_metadata_registration(context):
    jythonHookTestTool.log("post_metadata_registration")
    raise IllegalArgumentException("Fail at post_metadata_registration to cancel registration")

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-transaction.py")

