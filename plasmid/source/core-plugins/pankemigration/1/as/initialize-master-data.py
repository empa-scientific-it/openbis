#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##
## Configuration
##
#PATH_TO_MANAGE_PROPERTIES_SCRIPTS = "/Users/juanf/Documents/workspace/openbis/source/core-plugins/petermigration/1/compatibility/";
PATH_TO_MANAGE_PROPERTIES_SCRIPTS = "/Users/barillac/openbis-panke/servers/core-plugins/pankemigration/1/compatibility/";

# MasterDataRegistrationTransaction Class
import definitions
import definitionsVoc
import os
import copy
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

##
## Globals
##
vocabulariesCache = {};
propertiesCache = {};
samplesCache = {};
tr = service.transaction()

##
## API Facade
##
def createVocabularyWithTerms(vocabularyCode, terms):
    vocabulary = tr.createNewVocabulary(vocabularyCode);
    vocabulary.setChosenFromList(True);
    addTerms(vocabulary, terms);
    vocabulariesCache[vocabularyCode] = vocabulary;
    
def addTerms(vocabulary, terms):
    for term in terms:
        addTermWithLabel(vocabulary, term[0], term[1])
    
def addTermWithLabel(vocabulary, termCode, termLabel):
    newTerm = tr.createNewVocabularyTerm(termCode);
    newTerm.setLabel(termLabel);
    vocabulary.addTerm(newTerm);
    
def createSampleTypeWithProperties(sampleTypeCode, description, properties):
    newSampleType = tr.getOrCreateNewSampleType(sampleTypeCode);
    newSampleType.setDescription(description);
    newSampleType.setShowParents(True);
    newSampleType.setAutoGeneratedCode(True);
    newSampleType.setGeneratedCodePrefix(sampleTypeCode[:3]);
    addProperties(newSampleType, properties);
    samplesCache[sampleTypeCode] = newSampleType;
    
def createDataSetTypeWithProperties(dataSetCode, kind, description, properties):
    newDataSet = tr.getOrCreateNewDataSetType(dataSetCode);
    newDataSet.setDataSetKind(kind);
    newDataSet.setDescription(description);
    addProperties(newDataSet, properties);
    
def createExperimentTypeWithProperties(experimentTypeCode, description, properties):
    newExperiment = tr.getOrCreateNewExperimentType(experimentTypeCode);
    newExperiment.setDescription(description);
    addProperties(newExperiment, properties);

def addPropertiesToSamples(sampleTypeCodes, properties):
    for sampleTypeCode in sampleTypeCodes:
        sampleType = samplesCache[sampleTypeCode];
        addProperties(sampleType, properties);
    
def addProperties(entity, properties):
    for property in properties:
        propertyCode = property[0];
        if propertyCode.startswith("-"):
            continue
        elif propertyCode.startswith("+"):
            propertyCode = propertyCode[1:];
        
        addProperty(entity, propertyCode, property[1], property[2], property[3], property[4], property[5], property[6], property[7], property[8]);
    
def addProperty(entity, propertyCode, section, propertyLabel, dataType, vocabularyCode, propertyDescription, managedScript, dynamicScript, isMandatory):
    property = None;
    
    if propertyCode in propertiesCache:
        property = propertiesCache[propertyCode];
    else:
        property = createProperty(propertyCode, dataType, propertyLabel, propertyDescription, vocabularyCode);
    
    propertyAssignment = tr.assignPropertyType(entity, property);
    propertyAssignment.setSection(section);
    propertyAssignment.setMandatory(isMandatory);
    propertyAssignment.setShownEdit(True);
    
    if managedScript != None:
        propertyAssignment.setManaged(True);
        propertyAssignment.setScriptName(managedScript);
    if dynamicScript != None:
        propertyAssignment.setDynamic(True);
        propertyAssignment.setShownEdit(False);
        propertyAssignment.setScriptName(dynamicScript);

def createProperty(propertyCode, dataType, propertyLabel, propertyDescription, vocabularyCode):
    property = tr.getOrCreateNewPropertyType(propertyCode, dataType);
    property.setDescription(propertyDescription);
    property.setLabel(propertyLabel);
    propertiesCache[propertyCode] = property;
    if dataType == DataType.CONTROLLEDVOCABULARY:
        property.setVocabulary(vocabulariesCache[vocabularyCode]);
    return property;

#Valid Script Types: DYNAMIC_PROPERTY, MANAGED_PROPERTY, ENTITY_VALIDATION 
def createScript(path, name, description, scriptType, entityType):
    scriptAsString = open(path, 'r').read();
    script = tr.getOrCreateNewScript(name);
    script.setName(name);
    script.setDescription(description);
    script.setScript(scriptAsString);
    script.setScriptType(scriptType);
    script.setEntityForScript(entityType);
    return script;


def createAnnotationsScriptForType(sampleTypeCode):
    annotationsScriptName = None;
    if PATH_TO_MANAGE_PROPERTIES_SCRIPTS != None:
        annotationsScriptName = "ANNOTATIONS_" + sampleTypeCode;
        annotationsScriptAsString = open(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "managed.py", 'r').read();
        annotationsScriptAsString = annotationsScriptAsString.replace("<REPLACE_WITH_ANNOTABLE_TYPE>", sampleTypeCode);
        annotationsScript = tr.getOrCreateNewScript(annotationsScriptName);
        annotationsScript.setName(annotationsScriptName);
        annotationsScript.setDescription("Annotations Handler for " + sampleTypeCode);
        annotationsScript.setScript(annotationsScriptAsString);
        annotationsScript.setScriptType("MANAGED_PROPERTY");
        annotationsScript.setEntityForScript("SAMPLE");
    return annotationsScriptName;    
##
## Managed properties scripts
##
commentsScriptName = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "comments.py",
                                  definitions.commentsScriptName,
                                  "Comments Handler",
                                  "MANAGED_PROPERTY",
                                  "SAMPLE");

##
## Dynamic properties scripts
##


geneticModificationsScriptName = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "genetic_modifications.py",
                                  definitions.geneticModificationsScriptName,
                                  "genetic modifications",
                                  "DYNAMIC_PROPERTY",
                                  "SAMPLE");


    


##
## Vocabulary Types
##
for vocabularyCode, vocabularyValues in definitionsVoc.vocabularyDefinitions.iteritems():
    createVocabularyWithTerms(vocabularyCode, vocabularyValues)

##
## Experiment Types
##
createExperimentTypeWithProperties("DEFAULT_EXPERIMENT", "Default Experiment", definitions.experimentDefinition);
createExperimentTypeWithProperties("MATERIAL", "FOLDER FOR ORGANIZING MATERIALS SAMPLES", []);
createExperimentTypeWithProperties("METHOD", "FOLDER FOR ORGANIZING METHODS SAMPLES", []);


##
## Sample Types
##
#annotationsScriptName = createAnnotationsScriptForType("ANTIBODY");
#createSampleTypeWithProperties("ANTIBODY", "", definitions.antibodyDefinition);
#addStorageGroups(definitions.numberOfStorageGroups, "ANTIBODY");
annotationsScriptName = createAnnotationsScriptForType("YEAST");
createSampleTypeWithProperties("YEAST", "", definitions.strainDefinition);
# addRepetition(definitions.numberOfRepetitions, "STRAIN");
#===================================================================================================
# annotationsScriptName = createAnnotationsScriptForType("PLASMID");
# createSampleTypeWithProperties("PLASMID", "", definitions.plasmidDefinition);
# # addStorageGroups(definitions.numberOfStorageGroups, "PLASMID");
# annotationsScriptName = createAnnotationsScriptForType("OLIGO");
# createSampleTypeWithProperties("OLIGO", "", definitions.oligoDefinition);
# annotationsScriptName = createAnnotationsScriptForType("CHEMICAL");
# createSampleTypeWithProperties("CHEMICAL", "", definitions.chemicalDefinition);
# annotationsScriptName = createAnnotationsScriptForType("RESTRICTION_ENZYME");
# createSampleTypeWithProperties("RESTRICTION_ENZYME", "", definitions.RestrictionEnzymeDefinition);
annotationsScriptName = createAnnotationsScriptForType("EXPERIMENTAL_STEP");
createSampleTypeWithProperties("EXPERIMENTAL_STEP", "", definitions.ExperimentalStepDefinition);
#===================================================================================================


    
    