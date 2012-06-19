from ch.systemsx.cisd.etlserver.registrator import JythonHookTestTool

jythonHookTestTool = JythonHookTestTool.createInTest()

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-transaction.py")

transaction.getRegistrationContext().put("body","1")

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    jythonHookTestTool.log("rollback_transaction")

def pre_metadata_registration(context):
    
    assert_context_content(context, "pre_metadata_registration", "body", "1");
    assert_context_content(context, "pre_metadata_registration", "pre_metadata_registration", None);
    assert_context_content(context, "pre_metadata_registration", "post_storage", None);

    context.put("pre_metadata_registration", "2")
    jythonHookTestTool.log("pre_metadata_registration")

def post_metadata_registration(context):

    assert_context_content(context, "post_metadata_registration", "body", "1");
    assert_context_content(context, "post_metadata_registration", "pre_metadata_registration", "2");
    assert_context_content(context, "post_metadata_registration", "post_storage", None);

    context.put("post_metadata_registration", "3")
    jythonHookTestTool.log("post_metadata_registration")

def post_storage(context):

    assert_context_content(context, "post_storage", "body", "1");
    assert_context_content(context, "post_storage", "pre_metadata_registration", "2");
    assert_context_content(context, "post_storage", "post_metadata_registration", "3");

    context.put("post_storage", "4")
    jythonHookTestTool.log("post_storage")
    
def assert_context_content(context, caller, name, expected):
    value = context.get(name)
    if (value != expected):
        if (value != None and expected != None and type(value) != type(expected)):
            value = "%s:%s" % (type(value), value)
            expected = "%s:%s" % (type(expected), expected)
        jythonHookTestTool.log("transaction context failed. in %s the value of %s should have been '%s', but was '%s'" % (caller, name, expected, value) )
