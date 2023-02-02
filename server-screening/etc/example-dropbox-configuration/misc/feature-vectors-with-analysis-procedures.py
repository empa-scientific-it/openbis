#! /usr/bin/env python
# This is an example Jython dropbox for importing HCS image datasets

import os
import shutil
import random
from datetime import datetime

from java.io import File

def rollback_service(service, throwable):
	throwable.printStackTrace()

def rollback_transaction(service, transaction, algorithmRunner, throwable):
	pass


# type of the new analysis dataset
ANALYSIS_DATASET_TYPE = "HCS_IMAGE_ANALYSIS_DATA"

# sample type code of the plate, needed if a new sample is registered automatically 
PLATE_TYPE_CODE = "PLATE"
PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"
PLATE_GEOMETRY = "96_WELLS_8X12"

# The geometry of the plates
PLATE_ROWS = 8
PLATE_COLS = 12
		
# data set property constants 
ANALYSIS_PROCEDURE_PROPERTY_CODE = "$ANALYSIS_PROCEDURE"
	
def get_plate(tr, space_code, sample_code):
	""" Get the specified sample or register it if necessary """

	sampid = "/" + space_code + "/" + sample_code
	samp = tr.getSample(sampid)

	if None == samp:
		raise RuntimeError("Plate '%s' does not exist" % sample_code)
		
	return samp

def create_analysis_dataset_details(builder):
	return factory.createFeatureVectorRegistrationDetails(builder, None)
	
def generate_random_features(builder, analysisFeatureValue):
	# Create a feature for row number
	featureValues = builder.defineFeature("ROW_NUMBER")
	for row in range(1, PLATE_ROWS):
		for col in range(1, PLATE_COLS):
			value = row
			featureValues.addValue(row, col, str(value))

	# Create a feature for col number
	featureValues = builder.defineFeature("COLUMN_NUMBER")
	for row in range(1, PLATE_ROWS):
		for col in range(1, PLATE_COLS):
			value = col
			featureValues.addValue(row, col, str(value))
			
	featureValues = builder.defineFeature("PROCEDURE_ID")
	for row in range(1, PLATE_ROWS):
		for col in range(1, PLATE_COLS):
			value = analysisFeatureValue
			featureValues.addValue(row, col, str(value))
			
	featureCodes = ["NEGATIVE", "VALUES_WITH_NAN", "REAL", "INFECTION_INDEX", "TOTAL_CELLS"]
	index = 0
	for featureCode in featureCodes:
		featureValues = builder.defineFeature(featureCode)
		for row in range(1, PLATE_ROWS):
			for col in range(1, PLATE_COLS):
				value = random.gauss(-1 + index + 0.5, 0.25)
				#print featureCode, well, value
				featureValues.addValue(row, col, str(value))
		index = index + 1

def register_dataset(tr, sample, analysisProcedureCode, analysisFeatureValue):
	
	builder = factory.createFeaturesBuilder()
	generate_random_features(builder, analysisFeatureValue)
   
	# transform and move analysis file
	analysis_registration_details = create_analysis_dataset_details(builder)
	analysis_data_set = tr.createNewDataSet(analysis_registration_details)
	analysis_data_set.setSample(sample)
	if analysisProcedureCode:
	    analysis_data_set.setPropertyValue(ANALYSIS_PROCEDURE_PROPERTY_CODE, analysisProcedureCode)

	analysis_data_set_file = tr.createNewFile(analysis_data_set, "touched.data")
	print "Registering dataset:", analysis_data_set.getDataSetCode()

def register_random_feature_vectors(incoming, plate_code, with_procedures):
	
	tr = service.transaction(incoming)
	
	space_code = "PELKMANS_PUBLIC"
	data_sets_generated_per_analysis_procedure = 1
		
	sample = get_plate(tr, space_code, plate_code)
	
	if with_procedures:
		analysisProcedureWithFeature = { 
									"PROCEDURE-1" : "1.0",	
									"PROCEDURE-2" : "2.0",	
									"PROCEDURE-3" : "3.0", 
									"PROCEDURE-4" : "4.0" }
	else:
		analysisProcedureWithFeature = { }
	
	for analysisProcedure in analysisProcedureWithFeature:
		for data_set_index in range(0, data_sets_generated_per_analysis_procedure):
			analysisFeatureValue = analysisProcedureWithFeature[analysisProcedure] 
			register_dataset(tr, sample, analysisProcedure, analysisFeatureValue)
		
	# always register one data set *without* analysis procedure
	register_dataset(tr, sample, None, "17.125")
	
	tr.commit()

	# if the commit succeeded, we can delete the incoming file
	incoming.delete()
   
PLATES_WITH_PROCEDURES = """AD3-KY-NEW-CP071-1AA
AD3-KY-NEW-CP071-1AB
AD3-KY-NEW-CP071-1AC
AD3-KY-NEW-CP072-1AA
AD3-KY-NEW-CP072-1AB
AD3-KY-NEW-CP072-1AC
AD3-KY-NEW-CP073-1AA
AD3-KY-NEW-CP073-1AB
AD3-KY-NEW-CP073-1AC
AD3-MZ-NEW-CP071-1AA
AD3-MZ-NEW-CP071-1AB
AD3-MZ-NEW-CP071-1AC
AD3-MZ-NEW-CP072-1AA
AD3-MZ-NEW-CP072-1AB
AD3-MZ-NEW-CP072-1AC
AD3-MZ-NEW-CP073-1AA
AD3-MZ-NEW-CP073-1AB
AD3-MZ-NEW-CP073-1AC
AD5-KY-CP071-1AA
AD5-KY-CP071-1AB
AD5-KY-CP071-1AC
AD5-KY-CP072-1AA
AD5-KY-CP072-1AB
AD5-KY-CP072-1AC
AD5-KY-CP073-1AA
AD5-KY-CP073-1AB
AD5-KY-CP073-1AC
AD5-MZ-CP071-1AA
AD5-MZ-CP071-1AB
AD5-MZ-CP071-1AC
AD5-MZ-CP072-1AA
AD5-MZ-CP072-1AB
AD5-MZ-CP072-1AC
AD5-MZ-CP073-1AA
AD5-MZ-CP073-1AB
AD5-MZ-CP073-1AC
DV-KY2-CP071-1AA
DV-KY2-CP071-1AB
DV-KY2-CP071-1AC
DV-KY2-CP072-1AA
DV-KY2-CP072-1AB
DV-KY2-CP072-1AC
DV-KY2-CP073-1AA
DV-KY2-CP073-1AB
DV-KY2-CP073-1AC
DV-MZ-CP071-1AA
DV-MZ-CP071-1AB
DV-MZ-CP071-1AC
DV-MZ-CP072-1AA
DV-MZ-CP072-1AB
DV-MZ-CP072-1AC
DV-MZ-CP073-1AA
DV-MZ-CP073-1AB
DV-MZ-CP073-1AC
EV1-KY-CP071-1AA
EV1-KY-CP071-1AB
EV1-KY-CP071-1AC
EV1-KY-CP072-1AA
EV1-KY-CP072-1AB
EV1-KY-CP072-1AC
EV1-KY-CP073-1AA
EV1-KY-CP073-1AB
EV1-KY-CP073-1AC
EV1-MZ-CP071-1AA
EV1-MZ-CP071-1AB
EV1-MZ-CP071-1AC
EV1-MZ-CP072-1AA
EV1-MZ-CP072-1AB
EV1-MZ-CP072-1AC
EV1-MZ-CP073-1AA
EV1-MZ-CP073-1AB
EV1-MZ-CP073-1AC
HIV-MZ-2-CP071-1AA
HIV-MZ-2-CP071-1AB
HIV-MZ-2-CP071-1AC
HIV-MZ-2-CP072-1AA
HIV-MZ-2-CP072-1AB
HIV-MZ-2-CP072-1AC
HIV-MZ-2-CP073-1AA
HIV-MZ-2-CP073-1AB
HIV-MZ-2-CP073-1AC
HPV16-MZ-2-CP071-1AA
HPV16-MZ-2-CP071-1AB
HPV16-MZ-2-CP071-1AC
HPV16-MZ-2-CP072-1AA
HPV16-MZ-2-CP072-1AB
HPV16-MZ-2-CP072-1AC
HPV16-MZ-2-CP073-1AA
HPV16-MZ-2-CP073-1AB
HPV16-MZ-2-CP073-1AC
HRV2-KY-CP071-1AA
HRV2-KY-CP071-1AB
HRV2-KY-CP071-1AC
HRV2-KY-CP072-1AA
HRV2-KY-CP072-1AB
HRV2-KY-CP072-1AC
HRV2-KY-CP073-1AA
HRV2-KY-CP073-1AB
HRV2-KY-CP073-1AC
HRV2-MZ-CP071-1AA
HRV2-MZ-CP071-1AB
HRV2-MZ-CP071-1AC
HRV2-MZ-CP072-1AA
HRV2-MZ-CP072-1AB
HRV2-MZ-CP072-1AC
HRV2-MZ-CP073-1AA
HRV2-MZ-CP073-1AB
HRV2-MZ-CP073-1AC
HSV-KY-CP071-1AA
HSV-KY-CP071-1AC
HSV-KY-CP072-1AA
HSV-KY-CP072-1AB
HSV-KY-CP072-1AC
HSV-KY-CP073-1AA
HSV-KY-CP073-1AB
HSV-KY-CP073-1AC
HSV-MZ-CP071-1AA
HSV-MZ-CP071-1AB
HSV-MZ-CP071-1AC
HSV-MZ-CP072-1AA
HSV-MZ-CP072-1AB
HSV-MZ-CP072-1AC
HSV-MZ-CP073-1AG
HSV-MZ-CP073-1AH
HSV-MZ-CP073-1AI
IV-KY-CP071-1AA
IV-KY-CP071-1AB
IV-KY-CP071-1AC
IV-KY-CP072-1AA
IV-KY-CP072-1AB
IV-KY-CP072-1AC
IV-KY-CP073-1AA
IV-KY-CP073-1AB
IV-KY-CP073-1AC
IV-MZ-CP071-1AA
IV-MZ-CP071-1AB
IV-MZ-CP071-1AC
IV-MZ-CP072-1AA
IV-MZ-CP072-1AB
IV-MZ-CP072-1AC
IV-MZ-CP073-1AA
IV-MZ-CP073-1AB
IV-MZ-CP073-1AC
"""
PLATES_NO_PROCEDURES="""MHV-TDS-CP071-1AA
MHV-TDS-CP071-1AB
MHV-TDS-CP071-1AC
MHV-TDS-CP072-1AA
MHV-TDS-CP072-1AB
MHV-TDS-CP072-1AC
MHV-TDS-CP073-1AA
MHV-TDS-CP073-1AB
MHV-TDS-CP073-1AC
RRV-KY-CP071-1AA
RRV-KY-CP071-1AB
RRV-KY-CP071-1AC
RRV-KY-CP072-1AA
RRV-KY-CP072-1AB
RRV-KY-CP072-1AC
RRV-KY-CP073-1AA
RRV-KY-CP073-1AB
RRV-KY-CP073-1AC
RRV-MZ-CP071-1AA
RRV-MZ-CP071-1AB
RRV-MZ-CP071-1AC
RRV-MZ-CP072-1AA
RRV-MZ-CP072-1AB
RRV-MZ-CP072-1AC
RRV-MZ-CP073-1AA
RRV-MZ-CP073-1AB
RRV-MZ-CP073-1AC
RV-KY-2-CP071-1AA
RV-KY-2-CP071-1AB
RV-KY-2-CP071-1AC
RV-KY-2-CP072-1AB
RV-KY-2-CP072-1AC
RV-KY-2-CP072-1AD
RV-KY-2-CP073-1AB
RV-KY-2-CP073-1AC
RV-KY-2-CP073-1AD
RV-MZ-CP071-1AD
RV-MZ-CP071-1AE
RV-MZ-CP072-1AD
RV-MZ-CP072-1AE
RV-MZ-CP072-1AF
RV-MZ-CP073-1AD
RV-MZ-CP073-1AE
RV-MZ-CP073-1AF
SFV-KY-CP071-1AA
SFV-KY-CP071-1AB
SFV-KY-CP071-1AC
SFV-KY-CP072-1AA
SFV-KY-CP072-1AB
SFV-KY-CP072-1AC
SFV-KY-CP073-1AA
SFV-KY-CP073-1AC
SFV-KY-CP073-1AD
SFV-MZ-CP071-1AA
SFV-MZ-CP071-1AB
SFV-MZ-CP071-1AC
SFV-MZ-CP072-1AA
SFV-MZ-CP072-1AB
SFV-MZ-CP072-1AC
SFV-MZ-CP073-1AA
SFV-MZ-CP073-1AB
SFV-MZ-CP073-1AC
SV40-CNX-CP071-1AA
SV40-CNX-CP071-1AB
SV40-CNX-CP071-1AC
SV40-CNX-CP072-1AA
SV40-CNX-CP072-1AB
SV40-CNX-CP072-1AC
SV40-CNX-CP073-1AB
SV40-CNX-CP073-1AC
SV40-MZ-CP071-1AA
SV40-MZ-CP071-1AB
SV40-MZ-CP071-1AC
SV40-MZ-CP072-1AA
SV40-MZ-CP072-1AB
SV40-MZ-CP072-1AC
SV40-MZ-CP073-1AA
SV40-MZ-CP073-1AB
SV40-MZ-CP073-1AC
SV40-TDS-CP071-1AA
SV40-TDS-CP071-1AB
SV40-TDS-CP071-1AC
SV40-TDS-CP072-1AA
SV40-TDS-CP072-1AB
SV40-TDS-CP072-1AC
SV40-TDS-CP073-1AA
SV40-TDS-CP073-1AB
SV40-TDS-CP073-1AC
"""

for plate_code in PLATES_WITH_PROCEDURES.split():    
    register_random_feature_vectors(incoming, plate_code, True)

for plate_code in PLATES_NO_PROCEDURES.split():    
    register_random_feature_vectors(incoming, plate_code, False)
