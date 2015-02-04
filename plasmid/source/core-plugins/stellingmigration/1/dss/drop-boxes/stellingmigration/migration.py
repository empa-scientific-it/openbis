# some_file.py
from datetime import datetime
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria, SearchSubCriteria
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType
import xml.etree.ElementTree as ET

##
## Generic Process Method
##

definitions = {
               "GENERAL_PROTOCOL" : 
                                    { 
                                     "CHEMICALS" : {
                                                    "quantity" : "QUANTITY",
                                                    "name" : "NAME"
                                                    },
                                     "SOLUTIONS_BUFFERS" : {
                                                    "quantity" : "QUANTITY",
                                                    "name" : "NAME"
                                                    }
                                    }
};

def process(tr):
    print "START!"
    for sampleType in definitions:
        properties = definitions[sampleType]
        samples = getSamplesByType(tr, sampleType)
        print sampleType + ": "+ str(len(samples))
        for sample in samples:
            translate(tr, sample, properties)
    print "FINISH!"

def getSamplesByType(tr, sampleType):
    criteria = SearchCriteria()
    criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
    criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.TYPE, sampleType))
    samples = tr.getSearchService().searchForSamples(criteria)
    return samples

def getSampleByPermId(tr, permId):
    criteria = SearchCriteria()
    criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
    criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.PERM_ID, permId))
    samples = tr.getSearchService().searchForSamples(criteria)
    if len(samples) is 1:
        return samples[0]
    else:
        return None

def translate(tr, sample, properties):
    sampleType = sample.getSampleType()
    # Create new annotations from scratch
    newAnnotations = sample.getPropertyValue("ANNOTATIONS_STATE")
    newAnnotationsRoot = None
    newAnnotationsRoot = ET.Element("root")
    
    # Read old annotations
    for property in properties:
        propertyDefinitions = properties[property]
        oldAnnotationsRoot = None
        try:
            propertyValue = unicode(sample.getPropertyValue(property), "utf-8")
            if '<root>' in propertyValue:
                oldAnnotationsRoot = ET.fromstring(propertyValue)
        except Exception:
            print "Exception on " + sample.code + " " + property
        
        if oldAnnotationsRoot is not None:
            for child in oldAnnotationsRoot:
                    newAnnotationsNode = ET.SubElement(newAnnotationsRoot, "Sample")
                    permId = child.attrib["permId"]
                    print sample.code + " " + permId
                    newAnnotationsNode.attrib["permId"] = permId
                    linkedSample = getSampleByPermId(tr, permId)
                    newAnnotationsNode.attrib["identifier"] = linkedSample.getSampleIdentifier()
                    
                    for oldName in propertyDefinitions:
                        newName = propertyDefinitions[oldName]
                        value = getValueOrNull(child.attrib, oldName)
                        if(value is not None):
                            newAnnotationsNode.attrib[newName] = value
    save(tr, sample, "ANNOTATIONS_STATE", ET.tostring(newAnnotationsRoot, encoding='utf-8'))

def save(tr, sample, property, propertyValue):
    mutableSample = tr.makeSampleMutable(sample)
    mutableSample.setPropertyValue(property, propertyValue)
    
def getValueOrNull(map, key):
    if key in map:
        return map[key]
    else:
        return None