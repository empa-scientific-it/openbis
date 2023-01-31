from ch.systemsx.cisd.etlserver.registrator import JythonHookTestTool
jythonHookTestTool = JythonHookTestTool.createFromIncoming(incoming)

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    jythonHookTestTool.log("rollback_transaction")
	
def rollback_service(service, throwable):
    jythonHookTestTool.log("rollback_service")

tr = service.transaction(incoming, factory)
searchService = tr.getSearchService()
samples = searchService.searchForSamples("PROP", "VALUE", "SAMPLE_TYPE")
for sample in samples:
  tr.makeSampleMutable(sample)

