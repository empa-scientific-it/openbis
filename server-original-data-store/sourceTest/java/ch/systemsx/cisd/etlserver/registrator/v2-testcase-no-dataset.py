execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def process(transaction):
    mat = transaction.createNewMaterial("JTDH2_TEST", "GENE")