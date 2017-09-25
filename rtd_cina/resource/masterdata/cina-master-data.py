# -*- coding: utf-8 -*-

"""
  cina-master-data.py
  
  This script registers all master data for the CINA instance of openBIS
"""

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

tr = service.transaction()


file_type_HDF5 = tr.createNewFileFormatType('HDF5')
file_type_HDF5.setDescription('Hierarchical Data Format File, version 5')

file_type_PROPRIETARY = tr.createNewFileFormatType('PROPRIETARY')
file_type_PROPRIETARY.setDescription('Proprietary Format File')

file_type_SRF = tr.createNewFileFormatType('SRF')
file_type_SRF.setDescription('Sequence Read Format File')

file_type_TIFF = tr.createNewFileFormatType('TIFF')
file_type_TIFF.setDescription('TIFF File')

file_type_TSV = tr.createNewFileFormatType('TSV')
file_type_TSV.setDescription('Tab Separated Values File')

file_type_XML = tr.createNewFileFormatType('XML')
file_type_XML.setDescription('XML File')

vocabulary_STORAGE_FORMAT = tr.createNewVocabulary('STORAGE_FORMAT')
vocabulary_STORAGE_FORMAT.setDescription('The on-disk storage format of a data set')
vocabulary_STORAGE_FORMAT.setUrlTemplate(None)
vocabulary_STORAGE_FORMAT.setManagedInternally(True)
vocabulary_STORAGE_FORMAT.setInternalNamespace(True)
vocabulary_STORAGE_FORMAT.setChosenFromList(True)

vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY = tr.createNewVocabularyTerm('BDS_DIRECTORY')
vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY.setDescription(None)
vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY.setLabel(None)
vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY.setOrdinal(2)
vocabulary_STORAGE_FORMAT.addTerm(vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY)

vocabulary_term_STORAGE_FORMAT_PROPRIETARY = tr.createNewVocabularyTerm('PROPRIETARY')
vocabulary_term_STORAGE_FORMAT_PROPRIETARY.setDescription(None)
vocabulary_term_STORAGE_FORMAT_PROPRIETARY.setLabel(None)
vocabulary_term_STORAGE_FORMAT_PROPRIETARY.setOrdinal(1)
vocabulary_STORAGE_FORMAT.addTerm(vocabulary_term_STORAGE_FORMAT_PROPRIETARY)

exp_type_CINA_EXP_TYPE = tr.createNewExperimentType('CINA_EXP_TYPE')
exp_type_CINA_EXP_TYPE.setDescription('Generic Experiment Type')

samp_type_CINA_BROWSER_PREFERENCES = tr.createNewSampleType('CINA_BROWSER_PREFERENCES')
samp_type_CINA_BROWSER_PREFERENCES.setDescription('Browser Preferences.')
samp_type_CINA_BROWSER_PREFERENCES.setListable(True)
samp_type_CINA_BROWSER_PREFERENCES.setSubcodeUnique(False)
samp_type_CINA_BROWSER_PREFERENCES.setAutoGeneratedCode(False)
samp_type_CINA_BROWSER_PREFERENCES.setGeneratedCodePrefix('PREF-')

samp_type_GRID_PREP = tr.createNewSampleType('GRID_PREP')
samp_type_GRID_PREP.setDescription('Grid biochemistry and preparation')
samp_type_GRID_PREP.setListable(True)
samp_type_GRID_PREP.setSubcodeUnique(False)
samp_type_GRID_PREP.setAutoGeneratedCode(False)
samp_type_GRID_PREP.setGeneratedCodePrefix('EM-PREP')

samp_type_GRID_REPLICA = tr.createNewSampleType('GRID_REPLICA')
samp_type_GRID_REPLICA.setDescription('A replica of a grid preparation')
samp_type_GRID_REPLICA.setListable(True)
samp_type_GRID_REPLICA.setSubcodeUnique(False)
samp_type_GRID_REPLICA.setAutoGeneratedCode(False)
samp_type_GRID_REPLICA.setGeneratedCodePrefix('REPLICA')

data_set_type_ANALYSIS = tr.createNewDataSetType('ANALYSIS')
data_set_type_ANALYSIS.setDescription('Analysis')

data_set_type_BUNDLE = tr.createNewDataSetType('BUNDLE')
data_set_type_BUNDLE.setDescription('Bundle Data Set')

data_set_type_IMAGE = tr.createNewDataSetType('IMAGE')
data_set_type_IMAGE.setDescription('Annotated image')

data_set_type_METADATA = tr.createNewDataSetType('METADATA')
data_set_type_METADATA.setDescription('Metadata Data Set')

data_set_type_RAW_IMAGES = tr.createNewDataSetType('RAW_IMAGES')
data_set_type_RAW_IMAGES.setDescription('Raw Images')

prop_type_ANALYSIS_PROCEDURE = tr.createNewPropertyType('ANALYSIS_PROCEDURE', DataType.VARCHAR)
prop_type_ANALYSIS_PROCEDURE.setLabel('Analysis procedure')
prop_type_ANALYSIS_PROCEDURE.setManagedInternally(False)
prop_type_ANALYSIS_PROCEDURE.setInternalNamespace(True)


prop_type_ANNOTATION = tr.createNewPropertyType('ANNOTATION', DataType.MULTILINE_VARCHAR)
prop_type_ANNOTATION.setLabel('Annotation')
prop_type_ANNOTATION.setManagedInternally(False)
prop_type_ANNOTATION.setInternalNamespace(False)


prop_type_COLORFLAG = tr.createNewPropertyType('COLORFLAG', DataType.BOOLEAN)
prop_type_COLORFLAG.setLabel('Is Color?')
prop_type_COLORFLAG.setManagedInternally(False)
prop_type_COLORFLAG.setInternalNamespace(False)


prop_type_CREATION_DATE = tr.createNewPropertyType('CREATION_DATE', DataType.TIMESTAMP)
prop_type_CREATION_DATE.setLabel('Creation Date')
prop_type_CREATION_DATE.setManagedInternally(False)
prop_type_CREATION_DATE.setInternalNamespace(False)


prop_type_CREATOR_EMAIL = tr.createNewPropertyType('CREATOR_EMAIL', DataType.VARCHAR)
prop_type_CREATOR_EMAIL.setLabel('Creator Email')
prop_type_CREATOR_EMAIL.setManagedInternally(False)
prop_type_CREATOR_EMAIL.setInternalNamespace(False)


prop_type_DATATYPE = tr.createNewPropertyType('DATA-TYPE', DataType.VARCHAR)
prop_type_DATATYPE.setLabel('Data Type')
prop_type_DATATYPE.setManagedInternally(False)
prop_type_DATATYPE.setInternalNamespace(False)


prop_type_DESCRIPTION = tr.createNewPropertyType('DESCRIPTION', DataType.VARCHAR)
prop_type_DESCRIPTION.setLabel('Description')
prop_type_DESCRIPTION.setManagedInternally(False)
prop_type_DESCRIPTION.setInternalNamespace(False)


prop_type_DIMENSIONX = tr.createNewPropertyType('DIMENSIONX', DataType.INTEGER)
prop_type_DIMENSIONX.setLabel('Dimension X')
prop_type_DIMENSIONX.setManagedInternally(False)
prop_type_DIMENSIONX.setInternalNamespace(False)


prop_type_DIMENSIONY = tr.createNewPropertyType('DIMENSIONY', DataType.INTEGER)
prop_type_DIMENSIONY.setLabel('Dimension Y')
prop_type_DIMENSIONY.setManagedInternally(False)
prop_type_DIMENSIONY.setInternalNamespace(False)


prop_type_DIMENSIONZ = tr.createNewPropertyType('DIMENSIONZ', DataType.INTEGER)
prop_type_DIMENSIONZ.setLabel('Dimension Z')
prop_type_DIMENSIONZ.setManagedInternally(False)
prop_type_DIMENSIONZ.setInternalNamespace(False)


prop_type_LAB_ID = tr.createNewPropertyType('LAB_ID', DataType.VARCHAR)
prop_type_LAB_ID.setLabel('Lab ID')
prop_type_LAB_ID.setManagedInternally(False)
prop_type_LAB_ID.setInternalNamespace(False)


prop_type_MAX = tr.createNewPropertyType('MAX', DataType.REAL)
prop_type_MAX.setLabel('Max')
prop_type_MAX.setManagedInternally(False)
prop_type_MAX.setInternalNamespace(False)


prop_type_MICROSCOPE = tr.createNewPropertyType('MICROSCOPE', DataType.VARCHAR)
prop_type_MICROSCOPE.setLabel('Microscope')
prop_type_MICROSCOPE.setManagedInternally(False)
prop_type_MICROSCOPE.setInternalNamespace(False)


prop_type_MIN = tr.createNewPropertyType('MIN', DataType.REAL)
prop_type_MIN.setLabel('Min')
prop_type_MIN.setManagedInternally(False)
prop_type_MIN.setInternalNamespace(False)


prop_type_MISC = tr.createNewPropertyType('MISC', DataType.MULTILINE_VARCHAR)
prop_type_MISC.setLabel('All')
prop_type_MISC.setManagedInternally(False)
prop_type_MISC.setInternalNamespace(False)


prop_type_OPERATOR = tr.createNewPropertyType('OPERATOR', DataType.VARCHAR)
prop_type_OPERATOR.setLabel('Operator')
prop_type_OPERATOR.setManagedInternally(False)
prop_type_OPERATOR.setInternalNamespace(False)


prop_type_RATING = tr.createNewPropertyType('RATING', DataType.INTEGER)
prop_type_RATING.setLabel('Rating')
prop_type_RATING.setManagedInternally(False)
prop_type_RATING.setInternalNamespace(False)


prop_type_SIZEX = tr.createNewPropertyType('SIZEX', DataType.REAL)
prop_type_SIZEX.setLabel('Size X')
prop_type_SIZEX.setManagedInternally(False)
prop_type_SIZEX.setInternalNamespace(False)


prop_type_SIZEY = tr.createNewPropertyType('SIZEY', DataType.REAL)
prop_type_SIZEY.setLabel('Size Y')
prop_type_SIZEY.setManagedInternally(False)
prop_type_SIZEY.setInternalNamespace(False)


prop_type_SIZEZ = tr.createNewPropertyType('SIZEZ', DataType.REAL)
prop_type_SIZEZ.setLabel('Size Z')
prop_type_SIZEZ.setManagedInternally(False)
prop_type_SIZEZ.setInternalNamespace(False)


prop_type_STACKFLAG = tr.createNewPropertyType('STACKFLAG', DataType.BOOLEAN)
prop_type_STACKFLAG.setLabel('Is Stack?')
prop_type_STACKFLAG.setManagedInternally(False)
prop_type_STACKFLAG.setInternalNamespace(False)


assignment_DATA_SET_BUNDLE_MISC = tr.assignPropertyType(data_set_type_BUNDLE, prop_type_MISC)
assignment_DATA_SET_BUNDLE_MISC.setMandatory(False)
assignment_DATA_SET_BUNDLE_MISC.setSection(None)
assignment_DATA_SET_BUNDLE_MISC.setPositionInForms(1)

assignment_SAMPLE_GRID_PREP_DESCRIPTION = tr.assignPropertyType(samp_type_GRID_PREP, prop_type_DESCRIPTION)
assignment_SAMPLE_GRID_PREP_DESCRIPTION.setMandatory(False)
assignment_SAMPLE_GRID_PREP_DESCRIPTION.setSection(None)
assignment_SAMPLE_GRID_PREP_DESCRIPTION.setPositionInForms(1)

assignment_SAMPLE_GRID_REPLICA_DESCRIPTION = tr.assignPropertyType(samp_type_GRID_REPLICA, prop_type_DESCRIPTION)
assignment_SAMPLE_GRID_REPLICA_DESCRIPTION.setMandatory(False)
assignment_SAMPLE_GRID_REPLICA_DESCRIPTION.setSection(None)
assignment_SAMPLE_GRID_REPLICA_DESCRIPTION.setPositionInForms(1)

assignment_SAMPLE_GRID_REPLICA_CREATOR_EMAIL = tr.assignPropertyType(samp_type_GRID_REPLICA, prop_type_CREATOR_EMAIL)
assignment_SAMPLE_GRID_REPLICA_CREATOR_EMAIL.setMandatory(False)
assignment_SAMPLE_GRID_REPLICA_CREATOR_EMAIL.setSection(None)
assignment_SAMPLE_GRID_REPLICA_CREATOR_EMAIL.setPositionInForms(2)

assignment_DATA_SET_IMAGE_OPERATOR = tr.assignPropertyType(data_set_type_IMAGE, prop_type_OPERATOR)
assignment_DATA_SET_IMAGE_OPERATOR.setMandatory(False)
assignment_DATA_SET_IMAGE_OPERATOR.setSection(None)
assignment_DATA_SET_IMAGE_OPERATOR.setPositionInForms(1)

assignment_DATA_SET_IMAGE_ANNOTATION = tr.assignPropertyType(data_set_type_IMAGE, prop_type_ANNOTATION)
assignment_DATA_SET_IMAGE_ANNOTATION.setMandatory(False)
assignment_DATA_SET_IMAGE_ANNOTATION.setSection(None)
assignment_DATA_SET_IMAGE_ANNOTATION.setPositionInForms(2)

assignment_DATA_SET_IMAGE_RATING = tr.assignPropertyType(data_set_type_IMAGE, prop_type_RATING)
assignment_DATA_SET_IMAGE_RATING.setMandatory(False)
assignment_DATA_SET_IMAGE_RATING.setSection(None)
assignment_DATA_SET_IMAGE_RATING.setPositionInForms(3)

assignment_DATA_SET_IMAGE_DATATYPE = tr.assignPropertyType(data_set_type_IMAGE, prop_type_DATATYPE)
assignment_DATA_SET_IMAGE_DATATYPE.setMandatory(False)
assignment_DATA_SET_IMAGE_DATATYPE.setSection(None)
assignment_DATA_SET_IMAGE_DATATYPE.setPositionInForms(5)

assignment_DATA_SET_IMAGE_DIMENSIONX = tr.assignPropertyType(data_set_type_IMAGE, prop_type_DIMENSIONX)
assignment_DATA_SET_IMAGE_DIMENSIONX.setMandatory(False)
assignment_DATA_SET_IMAGE_DIMENSIONX.setSection('Dimension')
assignment_DATA_SET_IMAGE_DIMENSIONX.setPositionInForms(6)

assignment_DATA_SET_IMAGE_DIMENSIONY = tr.assignPropertyType(data_set_type_IMAGE, prop_type_DIMENSIONY)
assignment_DATA_SET_IMAGE_DIMENSIONY.setMandatory(False)
assignment_DATA_SET_IMAGE_DIMENSIONY.setSection('Dimension')
assignment_DATA_SET_IMAGE_DIMENSIONY.setPositionInForms(7)

assignment_DATA_SET_IMAGE_DIMENSIONZ = tr.assignPropertyType(data_set_type_IMAGE, prop_type_DIMENSIONZ)
assignment_DATA_SET_IMAGE_DIMENSIONZ.setMandatory(False)
assignment_DATA_SET_IMAGE_DIMENSIONZ.setSection('Dimension')
assignment_DATA_SET_IMAGE_DIMENSIONZ.setPositionInForms(8)

assignment_DATA_SET_IMAGE_SIZEX = tr.assignPropertyType(data_set_type_IMAGE, prop_type_SIZEX)
assignment_DATA_SET_IMAGE_SIZEX.setMandatory(False)
assignment_DATA_SET_IMAGE_SIZEX.setSection('Size')
assignment_DATA_SET_IMAGE_SIZEX.setPositionInForms(9)

assignment_DATA_SET_IMAGE_SIZEY = tr.assignPropertyType(data_set_type_IMAGE, prop_type_SIZEY)
assignment_DATA_SET_IMAGE_SIZEY.setMandatory(False)
assignment_DATA_SET_IMAGE_SIZEY.setSection('Size')
assignment_DATA_SET_IMAGE_SIZEY.setPositionInForms(10)

assignment_DATA_SET_IMAGE_SIZEZ = tr.assignPropertyType(data_set_type_IMAGE, prop_type_SIZEZ)
assignment_DATA_SET_IMAGE_SIZEZ.setMandatory(False)
assignment_DATA_SET_IMAGE_SIZEZ.setSection('Size')
assignment_DATA_SET_IMAGE_SIZEZ.setPositionInForms(11)

assignment_DATA_SET_IMAGE_MISC = tr.assignPropertyType(data_set_type_IMAGE, prop_type_MISC)
assignment_DATA_SET_IMAGE_MISC.setMandatory(False)
assignment_DATA_SET_IMAGE_MISC.setSection(None)
assignment_DATA_SET_IMAGE_MISC.setPositionInForms(12)

assignment_DATA_SET_METADATA_MISC = tr.assignPropertyType(data_set_type_METADATA, prop_type_MISC)
assignment_DATA_SET_METADATA_MISC.setMandatory(False)
assignment_DATA_SET_METADATA_MISC.setSection(None)
assignment_DATA_SET_METADATA_MISC.setPositionInForms(1)

assignment_DATA_SET_RAW_IMAGES_MISC = tr.assignPropertyType(data_set_type_RAW_IMAGES, prop_type_MISC)
assignment_DATA_SET_RAW_IMAGES_MISC.setMandatory(False)
assignment_DATA_SET_RAW_IMAGES_MISC.setSection(None)
assignment_DATA_SET_RAW_IMAGES_MISC.setPositionInForms(1)
