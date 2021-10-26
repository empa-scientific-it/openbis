#
# Generates a master data registration script for a running openBIS server.
#
# To execute it, please run the ./export-master-data.sh
#
import codecs
import sys
import re
import os
from datetime import datetime
from java.lang import System

ENCODING = "utf-8"

OUTPUT_DIR_PROP_KEY = "master.data.output.dir"

VARNAME_PREFIXES = {
  "EXPERIMENT" : "exp_type_",
  "SAMPLE" : "samp_type_",
  "DATA_SET" : "data_set_type_",
  "SCRIPT" : "script_",
  "MATERIAL" : "material_type_",
  "PROPERTY" : "prop_type_",
  "ASSIGNMENT" : "assignment_",
  "FILE_FORMAT" : "file_type_",
  "VOCABULARY" : "vocabulary_",
  "VOCABULARY_TERM" : "vocabulary_term_",
  "EXTERNAL_DATA_MANAGEMENT_SYSTEM" : "external_data_management_system_"
}

EXISTING_FILE_TYPES = ['HDF5', 'PROPRIETARY', 'SRF', 'TIFF', 'TSV', 'XML']
EXISTING_DATASET_TYPES = ['UNKNOWN']
EXISTING_VOCABULARY = {'$STORAGE_FORMAT' : ['PROPRIETARY', 'BDS_DIRECTORY']}

def getVarName(type, var):
    # remove minuses, dots and colons
    # they are not valid characters for Python variables
    normalized = re.sub("[\\-\\.:\\$]+", "", var)
    return VARNAME_PREFIXES[type] + normalized

def is_ascii(str):
    try:
        str.decode('ascii')
    except UnicodeDecodeError:
        return False

    return True

def strLiteral(var):
    def sanitize(dirtyString):
      return dirtyString.replace("'", "\\'")

    if var:
        # ASCII or UNICODE string?
        prefix = "'" if is_ascii(var) else "u'"
        return (prefix + sanitize(var) + "'").replace('\n', ' ')
    else:
        # NULL
        return 'None'

def scriptify(scriptBody):

  def escape_string(s):
      return '\\' + s

  returnstring = ""

  special_characters_dict = {'comment':"'''",   'newline':"\\n", 'carriage_return':"\\r",
                             'tab':"\\t", 'bell':"\\a", 'backspace':"\\b", 'formfeed':"\\f",
                             'vertical_tab':"\\v",  }

  lineIterator = iter(scriptBody.splitlines())
  for line in lineIterator:
    for special_character in special_characters_dict.values():
      line=line.replace(special_character, escape_string(special_character))
    returnstring += line + "\n"
  return ("'''" + returnstring + "'''")

def replaceSpace(stringWithSpaces):
  return stringWithSpaces.replace(" ", "_")

def codeLiteral(code):
    normalized = code
    if code[0] == "$":
        normalized = code[1:]
    return strLiteral(normalized)

def createValidationScriptAssigment(entityType, var):
    validationScript = entityType.getValidationScript()
    if (validationScript == None):
        return ""
    else:
        script = getVarName("SCRIPT", replaceSpace(validationScript.getName()))
        return "%s.setValidationScript(%s)" % (var, script)


def exportFileFormatType(fileType):
    var = getVarName("FILE_FORMAT", fileType.getCode())
    code = codeLiteral(fileType.getCode())
    description = strLiteral(fileType.getDescription())
    if (fileType.getCode() in EXISTING_FILE_TYPES):
        return ""
    else:
        return """
%(var)s = tr.getOrCreateNewFileFormatType(%(code)s)
%(var)s.setDescription(%(description)s)
   """ % vars()


def exportVocabulary(vocabulary):
    var = getVarName("VOCABULARY", vocabulary.getCode())
    code = codeLiteral(vocabulary.getCode())
    description = strLiteral(vocabulary.getDescription())
    urlTemplate = strLiteral(vocabulary.getUrlTemplate())
    isManagedInternally = vocabulary.isManagedInternally();
    isChosenFromList = vocabulary.isChosenFromList();
    if (vocabulary.getCode() in EXISTING_VOCABULARY):
        result = ""
    else:
        result = """    
%(var)s = tr.getOrCreateNewVocabulary(%(code)s)
%(var)s.setDescription(%(description)s)
%(var)s.setUrlTemplate(%(urlTemplate)s)
%(var)s.setManagedInternally(%(isManagedInternally)s)
%(var)s.setChosenFromList(%(isChosenFromList)s)
""" % vars()

    # dictionary for sorting the vocabulary terms
    termDict = {}

    for term in vocabulary.getTerms():
        term_var = getVarName("VOCABULARY_TERM", vocabulary.getCode() + "_" + term.getCode())
        term_description = strLiteral(term.getDescription())
        term_label = strLiteral(term.getLabel())
        if vocabulary.getCode() in EXISTING_VOCABULARY:
            if not (term.getCode() in EXISTING_VOCABULARY[vocabulary.getCode()]):
                termDict[int(term.getOrdinal())] = [term_var, term.getCode(), term_description, term_label]
        else:
             termDict[int(term.getOrdinal())] = [term_var, term.getCode(), term_description, term_label]

    if termDict:
        maxOrdinal = max(termDict.keys())
    else:
        maxOrdinal = 0
    for term in range(1, maxOrdinal + 1):
        if term in termDict:
            termvar = termDict[term][0]
            newTerm = codeLiteral(termDict[term][1])
            term_description = termDict[term][2]
            term_label = termDict[term][3]
            result = result + """          
%(termvar)s = tr.createNewVocabularyTerm(%(newTerm)s)
%(termvar)s.setDescription(%(term_description)s)
%(termvar)s.setLabel(%(term_label)s)
%(termvar)s.setOrdinal(%(term)s)
%(var)s.addTerm(%(termvar)s)
""" % vars()

    return result


def exportExperimentType(experimentType):
    var = getVarName("EXPERIMENT", experimentType.getCode())
    code = codeLiteral(experimentType.getCode())
    description = strLiteral(experimentType.getDescription())
    validationScript = createValidationScriptAssigment(experimentType, var)
    return """
%(var)s = tr.getOrCreateNewExperimentType(%(code)s)
%(var)s.setDescription(%(description)s)
%(validationScript)s
""" % vars()

def exportSampleType(sampleType):
    var = getVarName("SAMPLE", sampleType.getCode())
    code = codeLiteral(sampleType.getCode())
    description = strLiteral(sampleType.getDescription())
    listable = sampleType.isListable()
    showContainer = sampleType.isShowContainer()
    showParents = sampleType.isShowParents()
    isSubcodeUnique = sampleType.isSubcodeUnique()
    isAutoGeneratedCode = sampleType.isAutoGeneratedCode()
    isShowParentMetadata = sampleType.isShowParentMetadata()
    generatedCodePrefix = strLiteral(sampleType.getGeneratedCodePrefix())
    validationScript = createValidationScriptAssigment(sampleType, var)
    return """
%(var)s = tr.getOrCreateNewSampleType(%(code)s)
%(var)s.setDescription(%(description)s)
%(var)s.setListable(%(listable)s)
%(var)s.setShowContainer(%(showContainer)s)
%(var)s.setShowParents(%(showParents)s)
%(var)s.setSubcodeUnique(%(isSubcodeUnique)s)
%(var)s.setAutoGeneratedCode(%(isAutoGeneratedCode)s)
%(var)s.setShowParentMetadata(%(isShowParentMetadata)s)
%(var)s.setGeneratedCodePrefix(%(generatedCodePrefix)s)
%(validationScript)s
""" % vars()

def exportDataSetType(dataSetType):
    var = getVarName("DATA_SET", dataSetType.getCode())
    code = codeLiteral(dataSetType.getCode())
    description = strLiteral(dataSetType.getDescription())
    mainDataSetPattern = strLiteral(dataSetType.getMainDataSetPattern())
    mainDataSetPath = strLiteral(dataSetType.getMainDataSetPath())
    deletionDisallowed = dataSetType.isDeletionDisallowed()
    validationScript = createValidationScriptAssigment(dataSetType, var)
    if (dataSetType.getCode() in EXISTING_DATASET_TYPES):
        return ""
    else:
        return """
%(var)s = tr.getOrCreateNewDataSetType(%(code)s)
%(var)s.setDescription(%(description)s)
%(var)s.setMainDataSetPattern(%(mainDataSetPattern)s)
%(var)s.setMainDataSetPath(%(mainDataSetPath)s)
%(var)s.setDeletionDisallowed(%(deletionDisallowed)s)
%(validationScript)s
""" % vars()

def exportScripts (script):
  var = getVarName("SCRIPT", replaceSpace(script.getName()))
  name = strLiteral(replaceSpace(script.getName()))
  description = strLiteral(script.getDescription())
  scriptBody = scriptify(script.getScript())
  scriptType = strLiteral(script.getScriptType())
  scriptEntity = strLiteral(str(script.getEntity()))
  if str(script.getEntity()) == 'None':
    return """
%(var)s = tr.getOrCreateNewScript(%(name)s)
%(var)s.setName(%(name)s)
%(var)s.setDescription(%(description)s)
%(var)s.setScript(%(scriptBody)s)
%(var)s.setScriptType(%(scriptType)s)
""" % vars()
  else:
    return """
%(var)s = tr.getOrCreateNewScript(%(name)s)
%(var)s.setName(%(name)s)
%(var)s.setDescription(%(description)s)
%(var)s.setScript(%(scriptBody)s)
%(var)s.setEntityForScript(%(scriptEntity)s)
%(var)s.setScriptType(%(scriptType)s)
""" % vars()

def exportMaterialType(materialType):
    var = getVarName("MATERIAL", materialType.getCode())
    code = codeLiteral(materialType.getCode())
    description = strLiteral(materialType.getDescription())
    validationScript = createValidationScriptAssigment(materialType, var)
    return """
%(var)s = tr.getOrCreateNewMaterialType(%(code)s)
%(var)s.setDescription(%(description)s)
%(validationScript)s
""" % vars()

def exportPropertyType(propertyType):
    var = getVarName("PROPERTY", propertyType.getCode())
    code = codeLiteral(propertyType.getCode())
    label = strLiteral(propertyType.getLabel())
    dataType = propertyType.getDataType().name()
    description = strLiteral(propertyType.getDescription())
    specialSetters = ""

    if propertyType.getMaterialType():
        materialVar = getVarName("MATERIAL", propertyType.getMaterialType().getCode())
        specialSetters = "%(var)s.setMaterialType(%(materialVar)s)" % vars()

    if propertyType.getVocabulary():
        vocabularyVar = getVarName("VOCABULARY", propertyType.getVocabulary().getCode())
        specialSetters = specialSetters + "\n%(var)s.setVocabulary(%(vocabularyVar)s)" % vars()

    isManagedInternally = propertyType.isManagedInternally()

    return """
%(var)s = tr.getOrCreateNewPropertyType(%(code)s, DataType.%(dataType)s)
%(var)s.setLabel(%(label)s)
%(var)s.setManagedInternally(%(isManagedInternally)s)
%(var)s.setDescription(%(description)s)
%(specialSetters)s
""" % vars()

def exportAssignment(assignment):

    var = getVarName("ASSIGNMENT",
                     assignment.getEntityKind().name() + "_" +
                     assignment.getEntityTypeCode() + "_" +
                     assignment.getPropertyTypeCode())
    entityVar = getVarName(assignment.getEntityKind().name(), assignment.getEntityTypeCode())
    propertyVar = getVarName("PROPERTY", assignment.getPropertyTypeCode())
    isMandatory = assignment.isMandatory()
    section = strLiteral(assignment.getSection())
    posInForms = assignment.getPositionInForms()
    scriptName = strLiteral(assignment.getScriptName())
    isDynamic = assignment.isDynamic()
    isManaged = assignment.isManaged()
    isShownEdit = assignment.shownInEditViews()
    if str(assignment.getScriptName()) == 'None':
      return """
%(var)s = tr.assignPropertyType(%(entityVar)s, %(propertyVar)s)
%(var)s.setMandatory(%(isMandatory)s)
%(var)s.setSection(%(section)s)
%(var)s.setPositionInForms(%(posInForms)s)
%(var)s.setShownEdit(%(isShownEdit)s)
""" % vars()
    else:
      return """
%(var)s = tr.assignPropertyType(%(entityVar)s, %(propertyVar)s)
%(var)s.setMandatory(%(isMandatory)s)
%(var)s.setSection(%(section)s)
%(var)s.setPositionInForms(%(posInForms)s)
%(var)s.setScriptName(%(scriptName)s)
%(var)s.setDynamic(%(isDynamic)s)
%(var)s.setManaged(%(isManaged)s)
%(var)s.setShownEdit(%(isShownEdit)s)
""" % vars()


def exportExternalDataManagementSystem(edms):
    var = getVarName("EXTERNAL_DATA_MANAGEMENT_SYSTEM", edms.getCode())
    code = codeLiteral(edms.getCode())
    label = strLiteral(edms.getLabel())
    urlTemplate = strLiteral(edms.getUrlTemplate())
    isOpenBIS = edms.isOpenBIS()
    return """
%(var)s = tr.getOrCreateNewExternalDataManagementSystem(%(code)s)
%(var)s.setLabel(%(label)s)
%(var)s.setUrlTemplate(%(urlTemplate)s)
%(var)s.setOpenBIS(%(isOpenBIS)s)
""" % vars()

def defaultOutDirName():
  now = datetime.now()
  return "./exported-master-data-" + now.strftime("%y-%m-%d-%H-%M")


#
# Main script
#

outDir = System.getProperty(OUTPUT_DIR_PROP_KEY, defaultOutDirName())
print "Exporting master data to ", outDir, "..."


tr = service.transaction()

exportedContent = (
  [exportFileFormatType(fileType) for fileType in tr.listFileFormatTypes()] +
  ["""\nprint "Imported """ + str(len(tr.listFileFormatTypes())) + """ File Formats" """] +
  [exportVocabulary(vocabulary)   for vocabulary in tr.listVocabularies()] +
  ["""\nprint "Imported """ + str(len(tr.listVocabularies())) + """ Vocabularies" """] +
  [exportScripts(scripts) for scripts in tr.listScripts()] +
  ["""\nprint "Imported """ + str(len(tr.listScripts())) + """ Scripts" """] +
  [exportExperimentType(expType)  for expType in tr.listExperimentTypes()] +
  ["""\nprint "Imported """ + str(len(tr.listExperimentTypes())) + """ Experiment Types" """] +
  [exportSampleType(sampleType)   for sampleType in tr.listSampleTypes()] +
  ["""\nprint "Imported """ + str(len(tr.listSampleTypes())) + """ Sample Types" """] +
  [exportDataSetType(dataSetType) for dataSetType in tr.listDataSetTypes()] +
  ["""\nprint "Imported """ + str(len(tr.listDataSetTypes())) + """ Data Set Types" """] +
  [exportMaterialType(materialType) for materialType in tr.listMaterialTypes()] +
  ["""\nprint "Imported """ + str(len(tr.listMaterialTypes())) + """ Material Types" """] +
  [exportPropertyType(propertyType) for propertyType in tr.listPropertyTypes()] +
  ["""\nprint "Imported """ + str(len(tr.listPropertyTypes())) + """ Property Types" """] +
  [exportAssignment(assignment) for assignment in tr.listPropertyAssignments()] +
  ["""\nprint "Imported """ + str(len(tr.listPropertyAssignments())) + """ Property Assignments" """] +
  [exportExternalDataManagementSystem(edms) for edms in tr.listExternalDataManagementSystems()] +
  ["""\nprint "Imported """ + str(len(tr.listExternalDataManagementSystems())) + """ External DMSs" """]
);

if not os.path.exists(outDir):
    os.makedirs(outDir)

out = codecs.open(outDir + "/master-data.py", "w", ENCODING)
out.write("""# -*- coding: %(ENCODING)s -*-
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

print ("Importing Master Data...")

tr = service.transaction()

""" % vars())

exportedText = ''.join(exportedContent)
out.write(exportedText.encode(ENCODING))
out.write("\nprint (\"Import of Master Data finished.\") ")
out.close()

print "DONE"
