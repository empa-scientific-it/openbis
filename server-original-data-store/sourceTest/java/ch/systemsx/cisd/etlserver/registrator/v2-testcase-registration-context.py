from ch.systemsx.cisd.etlserver.registrator import JythonHookTestTool

jythonHookTestTool = JythonHookTestTool.createInTest()

def process(transaction):
  dataSet = transaction.createNewDataSet()
  transaction.moveFile(transaction.getIncoming().getPath() + '/sub_data_set_1', dataSet)
  dataSet.setDataSetType('O1')
  dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))
  transaction.getRegistrationContext().getPersistentMap().put("body","1")

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    jythonHookTestTool.log("rollback_transaction")

def pre_metadata_registration(context):
    
    assert_context_content(context, "pre_metadata_registration", "body", "1");
    assert_context_content(context, "pre_metadata_registration", "pre_metadata_registration", None);
    assert_context_content(context, "pre_metadata_registration", "post_storage", None);

    context.getPersistentMap().put("pre_metadata_registration", "2")
    jythonHookTestTool.log("pre_metadata_registration")

def post_metadata_registration(context):

    assert_context_content(context, "post_metadata_registration", "body", "1");
    assert_context_content(context, "post_metadata_registration", "pre_metadata_registration", "2");
    assert_context_content(context, "post_metadata_registration", "post_storage", None);

    context.getPersistentMap().put("post_metadata_registration", "3")
    jythonHookTestTool.log("post_metadata_registration")

def post_storage(context):

    assert_context_content(context, "post_storage", "body", "1");
    assert_context_content(context, "post_storage", "pre_metadata_registration", "2");
    assert_context_content(context, "post_storage", "post_metadata_registration", "3");

    context.getPersistentMap().put("post_storage", "4")
    jythonHookTestTool.log("post_storage")
    
def assert_context_content(context, caller, name, expected):
    value = context.getPersistentMap().get(name)
    if (value != expected):
        if (value != None and expected != None and type(value) != type(expected)):
            value = "%s:%s" % (type(value), value)
            expected = "%s:%s" % (type(expected), expected)
        jythonHookTestTool.log("transaction context failed.in %s the value of %s should have been '%s', but was '%s'" % (caller, name, expected, value) ) 
