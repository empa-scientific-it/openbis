ENTITY_KIND = "Entity Kind"
ENTITY_TYPE = "Entity Type"
PROPERTY_DEFINITION = "Property Definition"

def describe_property(property):
      return "%s %s %s %s %s %s %s %s %s" % ( 
        property.getPropertyTypeCode(), 
        property.getPropertyTypeDescription(), 
        property.getPropertyTypeLabel(), 
        property.isMandatory(), 
        property.getPositionInForms(), 
        property.isDynamic(), 
        property.isManaged(), 
        property.getScriptName(), 
        property.shownInEditViews())

def aggregate(parameters, tableBuilder):
    tableBuilder.addHeader(ENTITY_KIND)
    tableBuilder.addHeader(ENTITY_TYPE)
    tableBuilder.addHeader(PROPERTY_DEFINITION)
    
    #inner function that can access table builder
    def buildResult(properties, kind, type): 
        if len(properties) > 0:
            for property in properties:
                row = tableBuilder.addRow()
                row.setCell(ENTITY_KIND, kind)
                row.setCell(ENTITY_TYPE, type)
                row.setCell(PROPERTY_DEFINITION, describe_property(property))
                print describe_property(property)
        else:
            row = tableBuilder.addRow()
            row.setCell(ENTITY_KIND, kind)
            row.setCell(ENTITY_TYPE, type)
            row.setCell(PROPERTY_DEFINITION, "N/A")

    dataSetTypes = parameters.get("dataSetTypes") 
    for dataSetType in dataSetTypes:
        properties = searchService.listPropertiesDefinitionsForDataSetType(dataSetType)
        buildResult(properties, "Data Set", dataSetType)
        
    sampleTypes = parameters.get("sampleTypes") 
    for sampleType in sampleTypes:
        properties = searchService.listPropertiesDefinitionsForSampleType(sampleType)
        buildResult(properties, "Sample", sampleType)
        
    experimentTypes = parameters.get("experimentTypes") 
    for experimentType in experimentTypes:
        properties = searchService.listPropertiesDefinitionsForExperimentType(experimentType)
        buildResult(properties, "Experiment", experimentType)
        
    materialTypes = parameters.get("materialTypes") 
    for materialType in materialTypes:
        properties = searchService.listPropertiesDefinitionsForMaterialType(materialType)
        buildResult(properties, "Material", materialType)
        
        
        
        
        
        
        
        
        