
import os, glob, re, csv, time, shutil
from time import *
from datetime import *


#from ch.systemsx.cisd.openbis.dss.etl.dto.api.v2 import *
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v2 import SimpleFeatureVectorDataConfig
#from java.util import Properties 
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v2 import FeatureListDataConfig 
   

'''
Dropbox for importing a feature vector dataset and for creating feature lists datasets from there. 

This dataset is set to be a child of the segmentation dataset produced by Fethallah.

'''
print '###################################'
tz=localtime()[3]-gmtime()[3]
d=datetime.now()
print d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

accuracyA1_sirna_list = []
accuracyA2_sirna_list =[]
KStestA2_sirna_list = []
KStestA1_sirna_list = []
KSdeltaA2_sirna_list = []
KSdeltaA1_sirna_list = []
KSpvalueA2_sirna_list = []
KSpvalueA1_sirna_list = []
feature_directionA2_sirna_list = []
feature_directionA1_sirna_list = []
accuracyA1_gene_list = []
accuracyA2_gene_list =[]
KStestA2_gene_list = []
KStestA1_gene_list = []
KSdeltaA2_gene_list = []
KSdeltaA1_gene_list = []
KSpvalueA2_gene_list = []
KSpvalueA1_gene_list = []
feature_directionA2_gene_list = []
feature_directionA1_gene_list = []


def process(transaction):
	
	incoming = transaction.getIncoming()

	
# 	def copyTextFile(incoming):
# 	  for textfile in glob.glob(os.path.join(incoming, 'OriginalDataDirectory.txt')):
# 		rawDataFile = incoming + '/RawDataDirectory.txt'
# 		shutil.copyfile(textfile, rawDataFile)
# 	   
# 	copyTextFile(incoming.getPath())
	

#extract dataset code and plate of original image files from file OriginalDataDirectory.txt
	def extractImageDataSetCode(incoming):
		dataSetCode = ''
		plateCode = ''
		for textfile in glob.glob(os.path.join(incoming, 'OriginalDataDirectory.txt')):
		  text = open(textfile, "r")
		  lineIndex =0
		  for line in text:
			lineIndex=lineIndex+1
		 	if re.match('/raid', line):
		#	if re.match('/Users', line):
			  token_list = re.split(r"[/]",line)
			  token_list = [ item.strip() for item in token_list ]
			  token_list = filter(lambda x: len(x) > 0, token_list)
			  dataSetCode = token_list[8] #right position for raid is 8, for local use is 10
			if re.match('PLATE',line):
			  plateCode = line
		
		return dataSetCode, plateCode    
		  
	extractImageDataSetCode(incoming.getPath())

# check if plate code extracted above is the same as one of those in file  AnalysisFethallaExample_location.txt. If so, get the dataset code associated with that plate. This is the dataset 	
# that contains the analysis matlab files produced by Fethallah, which have been used by Riwal to perform his analysis, so the new dataset registered should be a child of Fethallah's dataset.
	def extractSegmentationDataSetCode(incoming):
		segmentationDataSetCode = ''
		segmentationPlateCode = ''
		for textfile in glob.glob(os.path.join(incoming, 'FethallahAnalysisOBLocation.txt')):
		  text = open(textfile, "r")
		  lineIndex =0
		  for line in text:
			lineIndex=lineIndex+1
			token_list = re.split(r"[\t]",line)
			token_list = [ item.strip() for item in token_list ]
			token_list = filter(lambda x: len(x) > 0, token_list)
			segmentationPlateCode = token_list[1]
			if (segmentationPlateCode == extractImageDataSetCode(incoming)[1].strip()):
				segmentationDataSetCode = token_list[0]
		
		return segmentationDataSetCode

	extractSegmentationDataSetCode(incoming.getPath())   
	
	def parse_gene_csv(incoming):
		for csv_file in glob.glob(os.path.join(incoming, 'A*gene.csv')):
			(dirName2, fileName2) = os.path.split(csv_file)
			(basename2, extension2) = os.path.splitext(fileName2)
			well_list = re.split(r"[_]",basename2)
			control = well_list[0]
			measure = well_list[1]
			csvfile = open(csv_file, "rb")
			test = csv.reader(csvfile, delimiter=',', quotechar='"')
			for i, row in enumerate(test):
				if i !=0:
					fnv = row[0]
					accuracy_value = row[1]
					KStest_value = row[2]
					KSdelta_value = row[3]
					KSpvalue_value = row[4]
					feature_direction_value = row[5]
					
					accuracyA2 = (fnv +"_G_ac_A2").upper()
					KStestA2 = (fnv+"_G_KSt_A2").upper()
					KSdeltaA2 = (fnv+"_G_KSd_A2").upper()
					KSpvalueA2 = (fnv+"_G_KSp_A2").upper()
					feature_directionA2 = (fnv+"_G_dir_A2").upper()
	
					accuracyA1 = (fnv +"_G_ac_A1").upper()
					KStestA1 = (fnv+"_G_KSt_A1").upper()
					KSdeltaA1 = (fnv+"_G_KSd_A1").upper()
					KSpvalueA1 = (fnv+"_G_KSp_A1").upper()
					feature_directionA1 = (fnv+"_G_dir_A1").upper()
	
					
					accuracyA2_gene_list.append(accuracyA2)
					accuracyA1_gene_list.append(accuracyA1)	
					KStestA2_gene_list.append(KStestA2)
					KStestA1_gene_list.append(KStestA1)
					KSdeltaA2_gene_list.append(KSdeltaA2)
					KSdeltaA1_gene_list.append(KSdeltaA1)
					KSpvalueA2_gene_list.append(KSpvalueA2)
					KSpvalueA1_gene_list.append(KSpvalueA1)
					feature_directionA2_gene_list.append(feature_directionA2)
					feature_directionA1_gene_list.append(feature_directionA1)
					
		return accuracyA2_gene_list, accuracyA1_gene_list, KStestA2_gene_list, KStestA1_gene_list, 	KSdeltaA2_gene_list, KSdeltaA1_gene_list, KSpvalueA2_gene_list, KSpvalueA1_gene_list, feature_directionA2_gene_list, feature_directionA1_gene_list
					
	parse_gene_csv(incoming.getPath())
	
	 

	def parse_sirna_csv(incoming):

		for csv_file in glob.glob(os.path.join(incoming, 'A*sirna.csv')):
			(dirName2, fileName2) = os.path.split(csv_file)
			(basename2, extension2) = os.path.splitext(fileName2)
			if re.search("-", basename2):
				continue
			else:
				well_list = re.split(r"[_]",basename2)
				control = well_list[0]
				measure = well_list[1]
				csvfile = open(csv_file, "rb")
				test = csv.reader(csvfile, delimiter=',', quotechar='"')
				for i, row in enumerate(test):
					if i !=0:
						fnv = row[0]
						accuracy_value = row[1]
						KStest_value = row[2]
						KSdelta_value = row[3]
						KSpvalue_value = row[4]
						feature_direction_value = row[5]
						
						accuracyA2 = (fnv +"_S_ac_A2").upper()
						KStestA2 = (fnv+"_S_KSt_A2").upper()
						KSdeltaA2 = (fnv+"_S_KSd_A2").upper()
						KSpvalueA2 = (fnv+"_S_KSp_A2").upper()
						feature_directionA2 = (fnv+"_S_dir_A2").upper()
		
						accuracyA1 = (fnv +"_S_ac_A1").upper()
						KStestA1 = (fnv+"_S_KSt_A1").upper()
						KSdeltaA1 = (fnv+"_S_KSd_A1").upper()
						KSpvalueA1 = (fnv+"_S_KSp_A1").upper()
						feature_directionA1 = (fnv+"_S_dir_A1").upper()
		
						
						accuracyA2_sirna_list.append(accuracyA2)
						accuracyA1_sirna_list.append(accuracyA1)	
						KStestA2_sirna_list.append(KStestA2)
						KStestA1_sirna_list.append(KStestA1)
						KSdeltaA2_sirna_list.append(KSdeltaA2)
						KSdeltaA1_sirna_list.append(KSdeltaA1)
						KSpvalueA2_sirna_list.append(KSpvalueA2)
						KSpvalueA1_sirna_list.append(KSpvalueA1)
						feature_directionA2_sirna_list.append(feature_directionA2)
						feature_directionA1_sirna_list.append(feature_directionA1)
					
		return accuracyA2_sirna_list, accuracyA1_sirna_list, KStestA2_sirna_list, KStestA1_sirna_list, 	KSdeltaA2_sirna_list, KSdeltaA1_sirna_list, KSpvalueA2_sirna_list, KSpvalueA1_sirna_list, feature_directionA2_sirna_list, feature_directionA1_sirna_list
					
	parse_sirna_csv(incoming.getPath())



		
	def defineGeneFeatures(featuresBuilder, incoming):
		for csv_file in glob.glob(os.path.join(incoming, 'global_gene.csv')):
			csvf = open(csv_file,'r')
			globcsv = csv.reader(csvf, delimiter=',')
			globcsv.next()
			result_accuracy = {} # accuracy_label => {measure_well => accuracy_value}	
			result_kstest ={} # kstest => {measure_well => kstest_value}
			result_ksdelta = {} # ksdelta => {measure_well => ksdelta_value}
			result_kspvalue ={} # kspvalue => {measure_well => kspvalue_value}
			result_feature_direction ={}# feature_direction => {measure_well => feature_direction_value}
			
			for row in globcsv:			
				measure_well 			= row[0]
				group_well 				= re.split(r"[-]",measure_well)
				group_well1				= group_well[0]
				control_well  			= row[1]
				feature_name			= row[3]
				accuracy_label  		= feature_name + "_G_ac"
				accuracy_value  		= row[4]
				kstest					= feature_name + "_G_KSt"
				kstest_value 			= row[5]
				ksdelta 				= feature_name + "_G_KSd"
				ksdelta_value 			= row[6]
				kspvalue				= feature_name + "_G_KSp"
				kspvalue_value			= row[7]
				feature_direction		= feature_name + "_G_dir"
				feature_direction_value	= row[8]
				
				
				accuracy_key = "%s:%s" %(accuracy_label, control_well)
				kstest_key = "%s:%s" %(kstest, control_well)
				ksdelta_key = "%s:%s" %(ksdelta, control_well)
				kspvalue_key ="%s:%s" %(kspvalue, control_well)
				feature_direction_key = "%s:%s" %(feature_direction, control_well)
				
				
				if not accuracy_key in result_accuracy:
					result_accuracy[accuracy_key] = {}
				
				result_accuracy[accuracy_key][group_well1] = accuracy_value
# 				if not kstest_key in result_kstest:
# 					result_kstest[kstest_key] = {}
# 					
# 				result_kstest[kstest_key][measure_well] = kstest_value
# 
# 				
# 				if not ksdelta_key in result_ksdelta:
# 					result_ksdelta[ksdelta_key] = {}
# 					
# 				result_ksdelta[ksdelta_key][measure_well] = ksdelta_value
# 
# 				if not kspvalue_key in result_kspvalue:
# 					result_kspvalue[kspvalue_key] = {}
# 					
# 				result_kspvalue[kspvalue_key][measure_well] = kspvalue_value
# 
# 
# 				if not feature_direction_key in result_feature_direction:
# 					result_feature_direction[feature_direction_key] = {}
# 					
# 				result_feature_direction[feature_direction_key][measure_well] = feature_direction_value



		for feature in result_accuracy:
			feature_accuracy = featuresBuilder.defineFeature(feature)
			for well in result_accuracy[feature]:
				value = result_accuracy[feature][well]
				feature_accuracy.addValue(well, value)
		
# 		for feature_kst in result_kstest:
# 			feature_kstest = featuresBuilder.defineFeature(feature_kst)
# 			for well2 in result_kstest[feature_kst]:
# 				value2 = result_kstest[feature_kst][well2]
# 				feature_kstest.addValue(well2, value2)
#  		
# 
# 		for feature_ksd in result_ksdelta:
# 			feature_ksdelta = featuresBuilder.defineFeature(feature_ksd)
# 			for well1 in result_ksdelta[feature_ksd]:
# 				value1 = result_ksdelta[feature_ksd][well1]
# 				feature_ksdelta.addValue(well1, value1)
#  		
# 		for feature_ksp in result_kspvalue:
# 			feature_kspvalue = featuresBuilder.defineFeature(feature_ksp)
# 			for well3 in result_kspvalue[feature_ksp]:
# 				value3 = result_kspvalue[feature_ksp][well3]
# 				feature_kspvalue.addValue(well3, value3)
# 
# 		for feature_fd in result_feature_direction:
# 			feature_feature_direction= featuresBuilder.defineFeature(feature_fd)
# 			for well4 in result_feature_direction[feature_fd]:
# 				value4 = result_feature_direction[feature_fd][well4]
# 				feature_feature_direction.addValue(well4, value4)		
		
		
		
		for csv_file2 in glob.glob(os.path.join(incoming, 'global_siRNA.csv')):
			csvf2 = open(csv_file2,'r')
			globcsv2 = csv.reader(csvf2, delimiter=',')
			globcsv2.next()
			result_accuracy_sirna = {} # accuracy_label => {measure_well => accuracy_value}	
			result_kstest_sirna ={} # kstest => {measure_well => kstest_value}
			result_ksdelta_sirna = {} # ksdelta => {measure_well => ksdelta_value}
			result_kspvalue_sirna ={} # kspvalue => {measure_well => kspvalue_value}
			result_feature_direction_sirna ={}# feature_direction => {measure_well => feature_direction_value}
			
			for row in globcsv2:			
				measure_well_sirna 				= row[0]
				control_well_sirna  			= row[1]
				feature_name_sirna				= row[4]
				accuracy_label_sirna  			= feature_name_sirna + "_S_ac"
				accuracy_value_sirna  			= row[5]
				kstest_sirna					= feature_name_sirna + "_S_KSt"
				kstest_value_sirna 				= row[6]
				ksdelta_sirna 					= feature_name_sirna + "_S_KSd"
				ksdelta_value_sirna 			= row[7]
				kspvalue_sirna					= feature_name_sirna + "_S_KSp"
				kspvalue_value_sirna			= row[8]
				feature_direction_sirna			= feature_name_sirna + "_S_dir"
				feature_direction_value_sirna	= row[9]
				
				
				accuracy_key_sirna = "%s:%s" %(accuracy_label_sirna, control_well_sirna)
				kstest_key_sirna = "%s:%s" %(kstest_sirna, control_well_sirna)
				ksdelta_key_sirna = "%s:%s" %(ksdelta_sirna, control_well_sirna)
				kspvalue_key_sirna ="%s:%s" %(kspvalue_sirna, control_well_sirna)
				feature_direction_key_sirna = "%s:%s" %(feature_direction_sirna, control_well_sirna)
				
				if not accuracy_key_sirna in result_accuracy_sirna:
					result_accuracy_sirna[accuracy_key_sirna] = {}
				result_accuracy_sirna[accuracy_key_sirna][measure_well_sirna] = accuracy_value_sirna




# 				if not kstest_key in result_kstest:
# 					result_kstest[kstest_key] = {}
# 					
# 				result_kstest[kstest_key][measure_well] = kstest_value
# 
# 				
# 				if not ksdelta_key in result_ksdelta:
# 					result_ksdelta[ksdelta_key] = {}
# 					
# 				result_ksdelta[ksdelta_key][measure_well] = ksdelta_value
# 
# 				if not kspvalue_key in result_kspvalue:
# 					result_kspvalue[kspvalue_key] = {}
# 					
# 				result_kspvalue[kspvalue_key][measure_well] = kspvalue_value
# 
# 
# 				if not feature_direction_key in result_feature_direction:
# 					result_feature_direction[feature_direction_key] = {}
# 					
# 				result_feature_direction[feature_direction_key][measure_well] = feature_direction_value

				
				
		for feature_sirna in result_accuracy_sirna:
			feature_accuracy_sirna = featuresBuilder.defineFeature(feature_sirna)
			for well_sirna in result_accuracy_sirna[feature_sirna]:
				value_sirna = result_accuracy_sirna[feature_sirna][well_sirna]
				feature_accuracy_sirna.addValue(well_sirna, value_sirna)

# 		for feature_kst in result_kstest:
# 			feature_kstest = featuresBuilder.defineFeature(feature_kst)
# 			for well2 in result_kstest[feature_kst]:
# 				value2 = result_kstest[feature_kst][well2]
# 				feature_kstest.addValue(well2, value2)
#  		
# 
# 		for feature_ksd in result_ksdelta:
# 			feature_ksdelta = featuresBuilder.defineFeature(feature_ksd)
# 			for well1 in result_ksdelta[feature_ksd]:
# 				value1 = result_ksdelta[feature_ksd][well1]
# 				feature_ksdelta.addValue(well1, value1)
#  		
# 		for feature_ksp in result_kspvalue:
# 			feature_kspvalue = featuresBuilder.defineFeature(feature_ksp)
# 			for well3 in result_kspvalue[feature_ksp]:
# 				value3 = result_kspvalue[feature_ksp][well3]
# 				feature_kspvalue.addValue(well3, value3)
# 
# 		for feature_fd in result_feature_direction:
# 			feature_feature_direction= featuresBuilder.defineFeature(feature_fd)
# 			for well4 in result_feature_direction[feature_fd]:
# 				value4 = result_feature_direction[feature_fd][well4]
# 				feature_feature_direction.addValue(well4, value4)

        		 	
  	config = SimpleFeatureVectorDataConfig()
  	featuresBuilder = config.featuresBuilder
  	defineGeneFeatures(featuresBuilder, incoming.getPath())
	analysisDataset = transaction.createNewFeatureVectorDataSet(config, incoming)
	

	rawImagesDataSetSample1 = transaction.getDataSet(extractSegmentationDataSetCode(incoming.getPath())).getSample()
	rawImagesDataSetSample = transaction.getSample('/SINERGIA/' + rawImagesDataSetSample1.getCode())

#        plateIdentifier = "/SINERGIA/PLATE1-G1-10X"
#        test = transaction.getSample("/SINERGIA/PLATE1-G1-10X")
#        analysisDataset.setSample(test)	

 	analysisDataset.setSample(rawImagesDataSetSample)
  
	search_service = transaction.getSearchService()
	
	sc = SearchCriteria()
	sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE,  extractSegmentationDataSetCode(incoming.getPath()) ));
	foundDataSets = search_service.searchForDataSets(sc)
	if foundDataSets.size() > 0:
	  analysisDataset.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])
  
     # store the original file in the dataset.
 	transaction.moveFile(incoming.getPath(), analysisDataset)





######################## Create Feature lists Datasets ###########################################

	config_accA2 = FeatureListDataConfig()
	config_accA2.setName("siRNA-based accuracy (reference well: A2)");
	config_accA2.setFeatureList(accuracyA2_sirna_list)
	config_accA2.setContainerDataSet(analysisDataset)
	transaction.createNewFeatureListDataSet(config_accA2)

#	config_accA1 = FeatureListDataConfig()
#	config_accA1.setName("siRNA-based accuracy (reference well: A1)");
#	config_accA1.setFeatureList(accuracyA1_sirna_list)
#	config_accA1.setContainerDataSet(analysisDataset)
#	transaction.createNewFeatureListDataSet(config_accA1)
#	
#	config_gene_accA2 = FeatureListDataConfig()
#	config_gene_accA2.setName("gene-based accuracy (reference well: A2)");
#	config_gene_accA2.setFeatureList(accuracyA2_gene_list)
#	config_gene_accA2.setContainerDataSet(analysisDataset)
#	transaction.createNewFeatureListDataSet(config_gene_accA2)
#
#	config_gene_accA1 = FeatureListDataConfig()
#	config_gene_accA1.setName("gene-based accuracy (reference well: A1)");
#	config_gene_accA1.setFeatureList(accuracyA1_gene_list)
#	config_gene_accA1.setContainerDataSet(analysisDataset)
#	transaction.createNewFeatureListDataSet(config_gene_accA1)
#	
#	
# 
# 	config_KStestA2 = FeatureListDataConfig()
# 	config_KStestA2.setName("KStest (reference well: A2)");
# 	config_KStestA2.setFeatureList(KStestA2_sirna_list)
# 	config_KStestA2.setContainerDataSet(analysisDataset)
# 	transaction.createNewFeatureListDataSet(config_KStestA2)
# 
# 	config_KStestA1 = FeatureListDataConfig()
# 	config_KStestA1.setName("KStest (reference well: A1)");
# 	config_KStestA1.setFeatureList(KStestA1_sirna_list)
# 	config_KStestA1.setContainerDataSet(analysisDataset)
# 	transaction.createNewFeatureListDataSet(config_KStestA1)
# 
# 
# 	config_KSdeltaA2 = FeatureListDataConfig()
# 	config_KSdeltaA2.setName("KSdelta (reference well: A2)");
# 	config_KSdeltaA2.setFeatureList(KSdeltaA2_sirna_list)
# 	config_KSdeltaA2.setContainerDataSet(analysisDataset)
# 	transaction.createNewFeatureListDataSet(config_KSdeltaA2)
# 
# 	config_KSdeltaA1 = FeatureListDataConfig()
# 	config_KSdeltaA1.setName("KSdelta (reference well: A1)");
# 	config_KSdeltaA1.setFeatureList(accuracyA1_sirna_list)
# 	config_KSdeltaA1.setContainerDataSet(analysisDataset)
# 	transaction.createNewFeatureListDataSet(config_KSdeltaA1)
# 
# 
# 	config_KSpvalueA2 = FeatureListDataConfig()
# 	config_KSpvalueA2.setName("KSpvalue (reference well: A2)");
# 	config_KSpvalueA2.setFeatureList(KSpvalueA2_sirna_list)
# 	config_KSpvalueA2.setContainerDataSet(analysisDataset)
# 	transaction.createNewFeatureListDataSet(config_KSpvalueA2)
# 
# 	config_KSpvalueA1 = FeatureListDataConfig()
# 	config_KSpvalueA1.setName("KSpvalue (reference well: A1)");
# 	config_KSpvalueA1.setFeatureList(KSpvalueA1_sirna_list)
# 	config_KSpvalueA1.setContainerDataSet(analysisDataset)
# 	transaction.createNewFeatureListDataSet(config_KSpvalueA1)
# 
# 	config_feature_directionA2 = FeatureListDataConfig()
# 	config_feature_directionA2.setName("Direction (reference well: A2)");
# 	config_feature_directionA2.setFeatureList(feature_directionA2_sirna_list)
# 	config_feature_directionA2.setContainerDataSet(analysisDataset)
# 	transaction.createNewFeatureListDataSet(config_feature_directionA2)
# 
# 	config_feature_directionA1 = FeatureListDataConfig()
# 	config_feature_directionA1.setName("Direction (reference well: A1)");
# 	config_feature_directionA1.setFeatureList(feature_directionA1_sirna_list)
# 	config_feature_directionA1.setContainerDataSet(analysisDataset)
# 	transaction.createNewFeatureListDataSet(config_feature_directionA1)

	

