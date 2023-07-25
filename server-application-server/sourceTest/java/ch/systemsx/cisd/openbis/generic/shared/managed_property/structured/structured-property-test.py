factory = elementFactory()
converter = propertyConverter()

#
# we use a function named "configureUI" to test something completely unrelated,
# because this is the easiest way to have the entire managed properties infrastrcture
# from the Java level 
#
def configureUI():

  # 
  # test creating the element structure
  #
  
  elements = [
      factory.createSampleLink("samplePermId"),
      factory.createMaterialLink("type", "typeCode"),
      factory.createElement("testElement").addAttribute("key1", "value1").addAttribute("key2", "value2")
  ]
  
  property.setValue(converter.convertToString(elements))


  
  #
  # test updating the property contents 
  #
  elements = list(converter.convertToElements(property))
  
  elements[0] = factory.createSampleLink("modifiedLink")
  elements[1].children = [
      factory.createElement("nested1").addAttribute("na1", "na2")
  ]
  elements[2].addAttribute("key2", "modifiedvalue")
  property.setValue(converter.convertToString(elements))
  

