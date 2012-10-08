# -*- coding: utf-8 -*-
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

print ("Importing Master Data...")

tr = service.transaction()

# File Formats
file_type_UNKNOWN = tr.getOrCreateNewFileFormatType('UNKNOWN')
file_type_UNKNOWN.setDescription('Unknown file format')
   

# Experiment Types
exp_type_5HT_EXP = tr.getOrCreateNewExperimentType('5HT_EXP')
exp_type_5HT_EXP.setDescription('A experiment against a 5HT receptor.')
print "Imported 1 Experiment Type" 

# Sample Types
samp_type_5HT_PROBE = tr.getOrCreateNewSampleType('5HT_PROBE')
samp_type_5HT_PROBE.setDescription('A probe of a 5HT receptor.')
samp_type_5HT_PROBE.setListable(True)
samp_type_5HT_PROBE.setShowContainer(False)
samp_type_5HT_PROBE.setShowParents(True)
samp_type_5HT_PROBE.setSubcodeUnique(False)
samp_type_5HT_PROBE.setAutoGeneratedCode(False)
samp_type_5HT_PROBE.setShowParentMetadata(False)
samp_type_5HT_PROBE.setGeneratedCodePrefix('5HT-')
print "Imported 1 Sample Type"

# Data Set Types
data_set_type_5HT_IMAGE = tr.getOrCreateNewDataSetType('5HT_IMAGE')
data_set_type_5HT_IMAGE.setDescription('Image data acquired in a 5HT experiment.')
data_set_type_5HT_IMAGE.setDataSetKind('PHYSICAL')
data_set_type_5HT_IMAGE.setMainDataSetPattern(None)
data_set_type_5HT_IMAGE.setMainDataSetPath(None)
data_set_type_5HT_IMAGE.setDeletionDisallowed(False)

data_set_type_5HT_TABLE = tr.getOrCreateNewDataSetType('5HT_TABLE')
data_set_type_5HT_TABLE.setDescription('Tabular data in a 5HT experiment')
data_set_type_5HT_TABLE.setDataSetKind('PHYSICAL')
data_set_type_5HT_TABLE.setMainDataSetPattern(None)
data_set_type_5HT_TABLE.setMainDataSetPath(None)
data_set_type_5HT_TABLE.setDeletionDisallowed(False)

data_set_type_5HT_UMBRELLA = tr.getOrCreateNewDataSetType('5HT_UMBRELLA')
data_set_type_5HT_UMBRELLA.setDescription('A data set type that groups other data sets for 5HT experiments')
data_set_type_5HT_UMBRELLA.setDataSetKind('CONTAINER')
data_set_type_5HT_UMBRELLA.setMainDataSetPattern(None)
data_set_type_5HT_UMBRELLA.setMainDataSetPath(None)
data_set_type_5HT_UMBRELLA.setDeletionDisallowed(False)
print "Imported 3 Data Set Types" 

# Materials
material_type_5HT_TARGET = tr.getOrCreateNewMaterialType('5HT_TARGET')
material_type_5HT_TARGET.setDescription('A target receptor of a 5HT experiment.')

material_type_5HT_COMPOUND = tr.getOrCreateNewMaterialType('5HT_COMPOUND')
material_type_5HT_COMPOUND.setDescription('A compound that has an effect on a 5HT receptor.')

print "Imported 2 Material Types" 

# Property Types
prop_type_DESC = tr.getOrCreateNewPropertyType('DESC', DataType.VARCHAR)
prop_type_DESC.setLabel('Description')
prop_type_DESC.setManagedInternally(False)
prop_type_DESC.setInternalNamespace(False)

prop_type_NAME = tr.getOrCreateNewPropertyType('NAME', DataType.VARCHAR)
prop_type_NAME.setLabel('Name')
prop_type_NAME.setManagedInternally(False)
prop_type_NAME.setInternalNamespace(False)

prop_type_PROT_NAME = tr.getOrCreateNewPropertyType('PROT_NAME', DataType.VARCHAR)
prop_type_PROT_NAME.setLabel('Protein Name')
prop_type_PROT_NAME.setManagedInternally(False)
prop_type_PROT_NAME.setInternalNamespace(False)

prop_type_GENE_NAME = tr.getOrCreateNewPropertyType('GENE_NAME', DataType.VARCHAR)
prop_type_GENE_NAME.setLabel('Gene Name')
prop_type_GENE_NAME.setManagedInternally(False)
prop_type_GENE_NAME.setInternalNamespace(False)

prop_type_LENGTH = tr.getOrCreateNewPropertyType('LENGTH', DataType.INTEGER)
prop_type_LENGTH.setLabel('Length')
prop_type_LENGTH.setManagedInternally(False)
prop_type_LENGTH.setInternalNamespace(False)

prop_type_CHEMBL = tr.getOrCreateNewPropertyType('CHEMBL', DataType.VARCHAR)
prop_type_CHEMBL.setLabel('Chembl Id')
prop_type_CHEMBL.setManagedInternally(False)
prop_type_CHEMBL.setInternalNamespace(False)

prop_type_TARGET = tr.getOrCreateNewPropertyType('TARGET', DataType.MATERIAL)
prop_type_TARGET.setLabel('Target')
prop_type_TARGET.setManagedInternally(False)
prop_type_TARGET.setInternalNamespace(False)
prop_type_TARGET.setMaterialType(material_type_5HT_TARGET)

prop_type_COMPOUND = tr.getOrCreateNewPropertyType('COMPOUND', DataType.MATERIAL)
prop_type_COMPOUND.setLabel('Compound')
prop_type_COMPOUND.setManagedInternally(False)
prop_type_COMPOUND.setInternalNamespace(False)
prop_type_COMPOUND.setMaterialType(material_type_5HT_COMPOUND)

print "Imported 7 Property Types" 

# Assignments
def assign(entity_type, property_type, position):
	assignment = tr.assignPropertyType(entity_type, property_type)
	assignment.setMandatory(False)
	assignment.setSection(None)
	assignment.setPositionInForms(position)
	
assign(samp_type_5HT_PROBE, prop_type_TARGET, 1)
assign(samp_type_5HT_PROBE, prop_type_COMPOUND, 2)
assign(samp_type_5HT_PROBE, prop_type_DESC, 3)

assign(material_type_5HT_TARGET, prop_type_NAME, 1)
assign(material_type_5HT_TARGET, prop_type_PROT_NAME, 2)
assign(material_type_5HT_TARGET, prop_type_GENE_NAME, 3)
assign(material_type_5HT_TARGET, prop_type_LENGTH, 4)
assign(material_type_5HT_TARGET, prop_type_CHEMBL, 5)
assign(material_type_5HT_TARGET, prop_type_DESC, 6)


print "Imported 9 Property Assignments" 
print ("Import of Master Data finished.") 