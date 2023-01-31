execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def post_metadata_registration(context, unnecessary_argument):
    jythonHookTestTool.log("post_metadata_registration")

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-transaction.py")

