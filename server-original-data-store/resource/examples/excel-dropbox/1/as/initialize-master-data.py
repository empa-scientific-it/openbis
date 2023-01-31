# -*- coding: utf-8 -*-
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

print ("Importing Master Data...")

tr = service.transaction()

# File Formats
file_type_UNKNOWN = tr.getOrCreateNewFileFormatType('UNKNOWN')
file_type_UNKNOWN.setDescription('Unknown file format')
   

# Experiment Types
exp_type_5HT_EXP = tr.getOrCreateNewExperimentType('EXCEL_EXAMPLE')
exp_type_5HT_EXP.setDescription('An example of an experiment defined in Excel.')
print "Imported 1 Experiment Type" 

# Property Types
prop_type_DESC = tr.getOrCreateNewPropertyType('DESC', DataType.VARCHAR)
prop_type_DESC.setLabel('Description')
prop_type_DESC.setManagedInternally(False)

print "Imported 1 Property Type" 

# Assignments
def assign(entity_type, property_type, position):
	assignment = tr.assignPropertyType(entity_type, property_type)
	assignment.setMandatory(False)
	assignment.setSection(None)
	assignment.setPositionInForms(position)
	
assign(exp_type_5HT_EXP, prop_type_DESC, 1)
