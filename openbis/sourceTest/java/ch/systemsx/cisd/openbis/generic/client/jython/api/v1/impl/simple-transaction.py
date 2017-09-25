import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

tr = service.transaction()

fileFormatType = tr.createNewFileFormatType('FILE-FORMAT-TYPE')
fileFormatType.setDescription('File format type description.')

animalsVocabulary = tr.createNewVocabulary('ANIMALS')
animalsVocabulary.setDescription("Vocabulary description")
animalsVocabulary.setUrlTemplate("http://ask.com/%s")

termTiger = tr.createNewVocabularyTerm("TIGER")
termTiger.setDescription("A wild cat")
termTiger.setLabel("Tiger")
animalsVocabulary.addTerm(termTiger)

termPuma = tr.createNewVocabularyTerm("PUMA")
termPuma.setDescription("Another wild cat")
termPuma.setLabel("Puma")
animalsVocabulary.addTerm(termPuma)

expType = tr.createNewExperimentType('EXPERIMENT-TYPE')
expType.setDescription('Experiment type description.')

sampleType = tr.createNewSampleType('SAMPLE-TYPE')
sampleType.setDescription('Sample type description.')
sampleType.setSubcodeUnique(True)
sampleType.setAutoGeneratedCode(True)
sampleType.setGeneratedCodePrefix("G_");

dataSetType = tr.createNewDataSetType('DATA-SET-TYPE')
dataSetType.setDescription('Data set type description.')

materialType = tr.createNewMaterialType('MATERIAL-TYPE')
materialType.setDescription('Material type description.')

stringPropertyType = tr.createNewPropertyType('VARCHAR-PROPERTY-TYPE', DataType.VARCHAR)
stringPropertyType.setDescription('Varchar property type description.')
stringPropertyType.setLabel('STRING')

materialPropertyType = tr.createNewPropertyType('MATERIAL-PROPERTY-TYPE', DataType.MATERIAL)
materialPropertyType.setDescription('Material property type description.')
materialPropertyType.setLabel('MATERIAL')
materialPropertyType.setMaterialType(materialType)
materialPropertyType.setManagedInternally(False)

assigment1 = tr.assignPropertyType(sampleType, materialPropertyType)
assigment1.setMandatory(True)

assigment2 = tr.assignPropertyType(expType, stringPropertyType)
assigment2.setMandatory(False)
assigment2.setDefaultValue("Default STRING Value")
