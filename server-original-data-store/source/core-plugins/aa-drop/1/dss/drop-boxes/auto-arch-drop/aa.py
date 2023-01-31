import random

def process(transaction):
  dataSet = transaction.createNewDataSet()
  incoming = transaction.getIncoming()
  transaction.moveFile(incoming.getAbsolutePath(), dataSet)

  if random.randint(0, 10) < 5:
      proj = ""
      exp = random.randint(5, 7)
  else:
      proj = "2"
      exp = random.randint(8, 10)
      
  dataSet.setExperiment(transaction.getExperiment("/ARCH/ARCH%s/E%d" % (proj, exp)))
	