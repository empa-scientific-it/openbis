from java.lang import IllegalArgumentException

transaction = service.transaction(incoming)


if incoming.getPath().endswith("-with-dyna-test"):
    print ">>>>>>> dyna test <<<<<<<"
    #test for LMS-2879
    sample = transaction.getSample("/CISD/NEMO/DYNA-TEST-1")    
    sa = transaction.getSampleForUpdate(sample.getSampleIdentifier())
    
    sa.setPropertyValue('COMMENT', 'comment')
    #try:
    
    #this set should fail. It doesn't as a leftover from LMS-2879
    sa.setPropertyValue('DYNAMIC_DESCRIPTION', 'description')
    #raise 'Setting of dynamic property should result in an exception'
    #except IllegalArgumentException, isx:
    #print isx
    #ok

dataSet = transaction.createNewDataSet()
exp = dataSet.getExperiment()
samp = dataSet.getSample()

if exp is None and samp is None:
  raise IllegalArgumentException("data-set-handler.py: No Experiment or Sample specified")
transaction.moveFile(incoming.getAbsolutePath(), dataSet)
