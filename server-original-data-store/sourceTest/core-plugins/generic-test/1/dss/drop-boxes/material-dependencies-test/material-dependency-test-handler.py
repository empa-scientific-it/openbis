
def process(transaction):
  sfrom = transaction.createNewMaterial("FROM", "SELF_REF")
  sto = transaction.createNewMaterial("TO", "SELF_REF")
  ofrom = transaction.createNewMaterial("FROM", "OTHER_REF")
  oto = transaction.createNewMaterial("TO", "OTHER_REF")
  
  # create links that have to be used in dependencies
  ofrom.setPropertyValue("ANY_MATERIAL", sto.getMaterialIdentifier())
  sfrom.setPropertyValue("ANY_MATERIAL", oto.getMaterialIdentifier())
  
  # create properties that should not be treated as linkds
  oto.setPropertyValue("DESCRIPTION", sfrom.getMaterialIdentifier())
  sto.setPropertyValue("DESCRIPTION", ofrom.getMaterialIdentifier())

  ofrom.setPropertyValue("DESCRIPTION", "IRRELEPHANT")
  sfrom.setPropertyValue("DESCRIPTION", "IRRELEPHANT")
  
  