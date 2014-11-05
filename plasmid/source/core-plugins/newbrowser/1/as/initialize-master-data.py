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

# MasterDataRegistrationTransaction Class
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

##
## Globals
##
vocabulariesCache = {};
propertiesCache = {};
tr = service.transaction()

##
## API Facade
##
def createVocabularyWithTerms(vocabularyCode, terms):
	vocabulary = tr.createNewVocabulary(vocabularyCode);
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
	newSampleType.setAutoGeneratedCode(True);
	newSampleType.setGeneratedCodePrefix(sampleTypeCode[:3]);
	addProperties(newSampleType, properties);
	
def createDataSetTypeWithProperties(dataSetCode, kind, description, properties):
	newDataSet = tr.getOrCreateNewDataSetType(dataSetCode);
	newDataSet.setDataSetKind(kind);
	newDataSet.setDescription(description);
	addProperties(newDataSet, properties);
	
def createExperimentTypeWithProperties(experimentTypeCode, description, properties):
	newExperiment = tr.getOrCreateNewExperimentType(experimentTypeCode);
	newExperiment.setDescription(description);
	addProperties(newExperiment, properties);
	
def addProperties(entity, properties):
	for property in properties:
		addProperty(entity, property[0], property[1], property[2], property[3], property[4], property[5]);
	
def addProperty(entity, propertyCode, section, propertyLabel, dataType, vocabularyCode, propertyDescription):
	property = None;
	
	if propertyCode in propertiesCache:
		property = propertiesCache[propertyCode];
	else:
		property = tr.getOrCreateNewPropertyType(propertyCode, dataType);
		property.setDescription(propertyDescription);
		property.setLabel(propertyLabel);
		propertiesCache[propertyCode] = property;
		if dataType == DataType.CONTROLLEDVOCABULARY:
			property.setVocabulary(vocabulariesCache[vocabularyCode]);
	
	propertyAssignment = tr.assignPropertyType(entity, property);
	propertyAssignment.setSection(section);

##
## Vocabulary Types
##
createVocabularyWithTerms("HOST", [
										["MOUSE", "mouse"],
										["RAT", "rat"],
										["GUINEA_PIG", "guinea pig"],
										["RABBIT", "rabbit"],
										["DONKEY", "donkey"]
									]);

createVocabularyWithTerms("DETECTION", [
										["HRP", "horseradish peroxydase"],
										["FLUORESCENCE", "fluorescent probe"]
									]);

createVocabularyWithTerms("STORAGE", [
										["RT", "room temperature"],
										["4", "+4 degrees"],
										["-20", "-20 degrees"],
										["-80", "-80 degrees"]
									]);

createVocabularyWithTerms("CLONALITY", [
										["MONOCLONAL", "monoclonal"],
										["POLYCLONAL", "polyclonal"],
										["UNKNOWN", "unknown"]
									]);

createVocabularyWithTerms("BACKBONE", [
										["PBLUESCRIPT_II_KS_PLUS", "pBluescript II KS +"],
										["PBSN", "pBSN"],
										["PSPPOLY_A", "pSPpoly(A)"],
										["PKERG10Y", "pKERG10y"],
										["PRS30Y", "pRS30y"],
										["PRS31Y", "pRS31y"],
										["PRS40Y", "pRS40y"],
										["PRS41Y", "pRS41y"],
										["PRS42Y", "pRS42y"],
										["PET22B", "pET22b"],
										["UNKNOWN", "unknown"],
										["PFA6", "pFA6"],
										["PGEX4T1", "pGEX4T1"],
										["PEG202", "pEG202"],
										["PJEXPRESS", "pJexpress"],
										["PJEXPRESS2", "pJexpress2"],
										["POLYLYS-PJEXPRESS2", "polyLys-pJexpress2"],
										["OSER", "OSER"]
									]);

createVocabularyWithTerms("BACTERIAL_ANTIBIOTIC_RESISTANCE", [
										["BLA", "bla"],
										["KAN", "kan"],
										["CAM", "cam"]
									]);

createVocabularyWithTerms("MARKER", [
										["URA3", "URA3"],
										["HIS3", "HIS3"],
										["LEU2", "LEU2"],
										["TRP1", "TRP1"],
										["MET15", "MET15"],
										["LYS2", "LYS2"],
										["ADE1", "ADE1"],
										["KANMX", "KanMX"],
										["NATMX", "NatMX"],
										["HYGMX", "HygMX"],
										["URA3MX", "Ura3MX"],
										["HIS3MX", "His3MX"],
										["BAR", "bar"],
										["CY_1", "Cy1"],
										["E_1", "e1"],
										["SB_1", "Sb1"],
										["W_1", "w1"],
										["Y1", "y1"],
									]);

createVocabularyWithTerms("STERILIZATION", [
										["AUTOCLAVATION", "autoclavation"],
										["FILTRATION", "filtration"],
										["NONE", "none"]
									]);

createVocabularyWithTerms("GENETIC_BACKGROUND", [
										["BY4743", "BY4743"],
										["BY4741", "BY4741"],
										["BY4742", "BY4742"],
										["CEN.PK2-1C", "CEN.PK2-1C"],
										["CEN.PK2-1D", "CEN.PK2-1D"],
										["CEN.PK2", "CEN.PK2"],
										["W303", "W303"],
										["W303-1A", "W303-1A"],
										["W303-1B", "W303-1B"],
										["S288C", "S288C"],
										["RM11", "RM11"],
										["RM11-A", "RM11-A"],
										["RM11-B", "RM11-B"],
										["UNKNOWN", "unknown"],
										["FY4", "FY4"]
									]);

createVocabularyWithTerms("MATING_TYPE", [
										["A", "a"],
										["ALPHA", "alpha"],
										["DIPLOID", "diploid"],
										["UNKNOWN", "unknown"]
									]);

createVocabularyWithTerms("BACKGROUND_SPECIFIC_MARKERS", [
										["MET15_LYS2", "met15- lys2-"],
										["MET15", "met15-"],
										["LYS2", "lys2-"],
										["TRP1_ADE2", "trp1- ade2-"],
										["TRP1", "trp1-"],
										["ADE2", "ade2-"],
										["MET15_TRP1", "met15- trp1-"],
										["HO_KAN", "ho::kanMX"],
										["NONE", "none"],
										["UNKNOWN", "unknown"],
										["MET15_LYS2_TRP1_ADE2", "met15- lys2- trp1- ade2-"],
										["LYS2_TRP1", "lys2- trp1-"],
										["MET15_LYS2_TRP1", "met15- lys2- trp1-"]
									]);

createVocabularyWithTerms("COMMON_MARKERS", [
										["URA3_HIS3_LEU2", "ura3- his3- leu2-"],
										["URA3_HIS3", "ura3- his3-"],
										["URA3_LEU2", "ura3- leu2-"],
										["URA3", "ura3-"],
										["HIS3_LEU2", "his3- leu2-"],
										["HIS3", "his3-"],
										["LEU2", "leu2-"],
										["NONE", "none"],
										["UNKNOWN", "unknown"]
									]);

createVocabularyWithTerms("ENDOGENOUS_PLASMID", [
										["CIR_PLUS", "cir+"],
										["CIR_ZERO", "cir0"],
										["UNKNOWN", "unknown"]
									]);

createVocabularyWithTerms("DIRECTION", [
										["FORWARD", "forward"],
										["REVERSE", "reverse"]
									]);

createVocabularyWithTerms("STRAND", [
										["DS", "double strand"],
										["SS", "single strand"]
									]);

createVocabularyWithTerms("RNA_TYPE", [
										["MIMIC", "mimic"],
										["INHIBITOR", "inhibitor"]
									]);

createVocabularyWithTerms("RNA_BACKBONE", [
										["LNA", "LNA"],
										["2_O_METHYL", "2-O-methylation"]
									]);

createVocabularyWithTerms("ORIGIN", [
										["CROSS", "cross"],
										["TRANSFORMATION", "transformation"],
										["SPORULATION", "transformation sporulation"],
										["NEGATIVE_SELECTION", "negative selection"],
										["TRANSFECTION", "transfection"]
									]);

createVocabularyWithTerms("CHECK", [
										["PCR", "PCR"],
										["MICROSCOPY", "microscopy"],
										["WB", "western blotting"],
										["SB", "southern blotting"],
										["PCR_MICROSCOPY", "PCR and microscopy"],
										["FLOWCYTOMETRY", "flow cytometry"],
										["PCR_FLOWCYTOMETRY", "PCR and flow cytometry"],
										["MORPHOLOGY", "morphology"],
										["OTHER", "other"],
										["NOTHING", "nothing"]
									]);

createVocabularyWithTerms("PROTOCOL_TYPE", [
										["DNA", "DNA method"],
										["RNA", "RNA method"],
										["PROTEINS", "proteins method"],
										["YEAST_BASICS", "yeast basic method"],
										["BACTERIA_BASICS", "bacteria basic method"],
										["FLUORESCENCE_MICROSCOPY", "fluorescence microscopy method"],
										["FLOW_CYTOMETRY", "flow cytometry method"],
										["CELL_SORTING", "cell sorting method"],
										["CELL_LINE_BASICS", "cell line basics"]
									]);

createVocabularyWithTerms("TEMPLATE", [
										["DNA", "DNA"],
										["RNA", "RNA"],
										["BACTERIA_COLONY", "bacteria colony"],
										["YEAST_COLONY", "yeast colony"]
									]);

createVocabularyWithTerms("YES_NO", [
										["YES", "yes"],
										["NO", "no"],
										["UNKNOWN", "unknown"]
									]);

createVocabularyWithTerms("MEMBRANE", [
										["PVDF", "PVDF"],
										["NITROCELLULOSE", "nitrocellulose"],
										["PVDF_NITROCELLULOSE", "Either PVDF or nitrocellulose"]
									]);

createVocabularyWithTerms("SPECIES", [
										["HOMO", "Homo sapiens"],
										["MOUSE", "Mus musculus"],
										["RAT", "Rattus norvegicus"],
										["PIG", "Sus scrofa"],
										["DROSOPHILA_MELANOGASTER", "Drosophila melanogaster"]
									]);

createVocabularyWithTerms("CELL_MEDIUM", [
										["RPMI", "rpmi"],
										["1640", "1640"],
										["ISCOVES", "iscoves"],
										["DMEM", "DMEM"],
										["DMEM_NUTRIENT_MIXTURE_F-12_HAM", "DMEM nutrient mixture F-12 HAM"],
										["DMEM_HIGH_GLUC", "DMEM high glucose"],
										["DMEM_LOW_GLUC", "DMEM low glucose"]
									]);

createVocabularyWithTerms("OWNER", [
										["FILL_ME_1", "Fill me with the people of your lab"],
										["FILL_ME_2", "Fill me with the people of your lab 2"]
									]);

createVocabularyWithTerms("CELL_TYPE", [
										["FIBROBLAST", "fibroblast"],
										["NEURON", "neuron"]
									]);

createVocabularyWithTerms("ORGANISM", [
										["BACTERIA", "Bacteria"],
										["BUDDING_YEAST", "Saccharomyces cerevisiae"],
										["MAMMALIAN", "mammalian"],
										["DROSOPHILA_MELANOGASTER", "Drosophila melanogaster"]
									]);

createVocabularyWithTerms("EXPERIMENTAL_READOUT", [
										["FLOW_CYTOMETRY", "flow citometry"],
										["SORTING", "cell sorting"],
										["GROWTH", "growth"],
										["WESTERN_BLOTTING", "western blottong"],
										["RT_QPCR", "RT-qPCR"]
									]);

createVocabularyWithTerms("MACHINE", [
										["LSRII_FORTESSA", "SRII Fortessa"],
										["TECAN_READER", "Tecan reader"],
										["BIOLECTOR", "BioLector"],
										["LICOR_ODYSSEY", "LI-COR Odyssey"],
										["TI_ECLIPSE", "TI Eclipse (Nikon)"],
										["SRX_101A", "Konica Minolta SRX-101A"],
										["LIGHT_CYCLER", "LightCycler 480"]
									]);

##
## DataSet Types
##
createDataSetTypeWithProperties("ELN_PREVIEW", "PHYSICAL", "ELN Preview image", []);

createDataSetTypeWithProperties("SEQ_FILE", "PHYSICAL", "", [
	["NOTES", "General information", "Notes", DataType.MULTILINE_VARCHAR, None, "Notes regarding the dataset"],
]);

createDataSetTypeWithProperties("RAW_DATA", "PHYSICAL", "", [
	["NOTES", "General information", "Notes", DataType.MULTILINE_VARCHAR, None, "Notes regarding the dataset"],
]);

##
## Experiment Types
##
createExperimentTypeWithProperties("ANTIBODY", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("BACTERIA", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("CHEMICAL", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("ENZYME", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("MEDIA", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("OLIGO", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("RNA", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("PLASMID", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("SOLUTION_BUFFER", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("YEAST", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("CELL_LINE", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("FLY", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);

createExperimentTypeWithProperties("GENERAL_PROTOCOL", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("PCR_PROTOCOL", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("WESTERN_BLOTTING_PROTOCOL", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);

createExperimentTypeWithProperties("DEFAULT_EXPERIMENT", "Default Experiment", [
	["NAME", 				"General", "Name", 					DataType.VARCHAR, 			None,	"Name"],
	["EXPERIMENTAL_GOALS", 	"General", "Experimental goals", 	DataType.MULTILINE_VARCHAR, None,	"Goal of the experiment"],
	["GRANT", 				"General", "Grant", 				DataType.VARCHAR,			None,	"grant name"],
	["START_DATE", 			"General", "Start Date", 			DataType.TIMESTAMP, 		None,	"Start Date"],
	["END_DATE", 			"General", "End Date", 				DataType.TIMESTAMP,			None,	"End Date"],
	["EXPERIMENTAL_RESULTS","General", "Experimental results", 	DataType.MULTILINE_VARCHAR, None,	"Brief summary of the results obtained"],
	["XMLCOMMENTS",			"Comments","Comments List",			DataType.XML,				None,	"Several comments can be added by different users"]
]);

##
## Sample Types
##
createSampleTypeWithProperties("ANTIBODY", "", [
	["NAME", 				"General", 				"Name", 				DataType.VARCHAR,				None,		"Name"],
	["HOST", 				"General", 				"Host", 				DataType.CONTROLLEDVOCABULARY,	"HOST", 	"Host used to produce the antibody"],
	["FOR_WHAT", 			"General", 				"For what", 			DataType.MULTILINE_VARCHAR,		None, 		"For what kind of experimental application/readout this sample is used in the lab"],
	["DETECTION", 			"General", 				"Detection",			DataType.CONTROLLEDVOCABULARY,	"DETECTION","Protein detection system (fill in this information only for secondary antibodies)"],
	["EPITOPE", 			"General", 				"Epitope",				DataType.MULTILINE_VARCHAR,		None, 		"Epitope of the antibody"],
	["CLONALITY", 			"General", 				"Clonality",			DataType.CONTROLLEDVOCABULARY,	"CLONALITY","Clonality of the antibody"],
	["ISOTYPE", 			"General", 				"Isotype", 				DataType.MULTILINE_VARCHAR,		None, 		"Isotype of the antibody"],
	["SUPPLIER", 			"Supplier and storage", "Supplier",				DataType.MULTILINE_VARCHAR,		None, 		"Supplier of the product"],
	["ARTICLE_NUMBER", 		"Supplier and storage", "Art. Number", 			DataType.MULTILINE_VARCHAR,		None, 		"Article number of the product"],
	["STORAGE", 			"Supplier and storage", "Storage", 				DataType.CONTROLLEDVOCABULARY,	"STORAGE", 	"Storage conditions of the product"],
	["STOCK_CONCENTRATION", "Supplier and storage", "Stock concentration", 	DataType.VARCHAR,				None, 		"Stock concentration of the solution where the product is kept in the lab"],
	["PUBLICATION", 		"Comments", 			"Publication", 			DataType.MULTILINE_VARCHAR,		None, 		"Publication from where the information was first found OR technical sheet given by the manufacturer"],
	["NOTES", 				"Comments", 			"Notes", 				DataType.MULTILINE_VARCHAR,		None, 		"Notes"],
	["XMLCOMMENTS",			"Comments",				"Comments List",		DataType.XML,					None,		"Several comments can be added by different users"]
]);

createSampleTypeWithProperties("CHEMICAL", "", [
	["NAME", 				"General", 				"Name", 				DataType.MULTILINE_VARCHAR,		None,		"Name"],
	["SUPPLIER", 			"Supplier and storage", "Supplier", 			DataType.MULTILINE_VARCHAR,		None,		"Supplier of the product"],
	["ARTICLE_NUMBER", 		"Supplier and storage", "Art. Number",			DataType.MULTILINE_VARCHAR,		None,		"Article number of the product"],
	["STORAGE", 			"Supplier and storage", "Storage", 				DataType.CONTROLLEDVOCABULARY,	"STORAGE",	"Storage conditions of the product"],
	["XMLCOMMENTS", 		"Comments", 			"Comments List", 		DataType.XML,					None,		"Several comments can be added by different users"]
]);

createSampleTypeWithProperties("ENZYME", "", [
	["NAME", 				"General",				"Name",					DataType.MULTILINE_VARCHAR,		None,		"Name"],
	["SUPPLIER", 			"Supplier and storage",	"Supplier",				DataType.MULTILINE_VARCHAR,		None,		"Supplier of the product"],
	["ARTICLE_NUMBER", 		"Supplier and storage",	"Art. Number",			DataType.MULTILINE_VARCHAR,		None,		"Article number of the product"],
	["KIT", 				"Supplier and storage",	"Kit including",		DataType.MULTILINE_VARCHAR,		None,		"What the company includes with the enzyme"],
	["STORAGE",				"Supplier and storage",	"Storage",				DataType.CONTROLLEDVOCABULARY,	"STORAGE",	"Storage conditions of the product"],
	["XMLCOMMENTS",			"Comments",				"Comments List",		DataType.XML,					None,		"Several comments can be added by different users"]
]);

createSampleTypeWithProperties("MEDIA", "", [
	["NAME", 				"General",				"Name",					DataType.MULTILINE_VARCHAR,		None,			"Name"],
	["FOR_WHAT", 			"General",				"For what",				DataType.MULTILINE_VARCHAR,		None,			"For what kind of experimental application/readout this sample is used in the lab"],
	["ORGANISM", 			"General",				"Organism",				DataType.CONTROLLEDVOCABULARY,	"ORGANISM",		"For what organism this medium is used"],
	["STORAGE", 			"Storage",				"Storage",				DataType.CONTROLLEDVOCABULARY,	"STORAGE",		"Storage conditions of the product"],
	["STOCK_CONCENTRATION", "Storage",				"Stock concentration",	DataType.VARCHAR,				None,			"Stock concentration of the solution where the product is kept in the lab"],
	["STERILIZATION", 		"Storage",				"Sterilization",		DataType.CONTROLLEDVOCABULARY,	"STERILIZATION","How the solution/buffer is sterilized when prepared"],
	["PUBLICATION", 		"Comments",				"Publication",			DataType.MULTILINE_VARCHAR,		None,			"Publication from where the information was first found OR technical sheet given by the manufacturer"],
	["NOTES", 				"Comments",				"Notes",				DataType.MULTILINE_VARCHAR,		None,			"Deatails for solution/buffer preparation"],
	["XMLCOMMENTS", 		"Comments",				"Comments List",		DataType.XML,					None,			"Several comments can be added by different users"]
]);

createSampleTypeWithProperties("SOLUTION_BUFFER", "", [
	["NAME", 				"General",				"Name",					DataType.MULTILINE_VARCHAR,		None,			"Name"],
	["FOR_WHAT", 			"General",				"For what",				DataType.MULTILINE_VARCHAR,		None,			"For what kind of experimental application/readout this sample is used in the lab"],
	["DETAILS", 			"Recipe",				"Details",				DataType.MULTILINE_VARCHAR,		None,			"Details and tips about how to prepare the solution/buffer"],
	["STORAGE", 			"Storage",				"Storage",				DataType.CONTROLLEDVOCABULARY,	"STORAGE",		"Storage conditions of the product"],
	["STOCK_CONCENTRATION", "Storage",				"Stock concentration",	DataType.VARCHAR,				None,			"Stock concentration of the solution where the product is kept in the lab"],
	["STERILIZATION", 		"Storage",				"Sterilization",		DataType.CONTROLLEDVOCABULARY,	"STERILIZATION","How the solution/buffer is sterilized when prepared"],
	["PUBLICATION", 		"Comments",				"Publication",			DataType.MULTILINE_VARCHAR,		None,			"Publication from where the information was first found OR technical sheet given by the manufacturer"],
	["NOTES", 				"Comments",				"Notes",				DataType.MULTILINE_VARCHAR,		None,			"Notes"],
	["XMLCOMMENTS", 		"Comments",				"Comments List",		DataType.XML,					None,			"Several comments can be added by different users"]
]);

createSampleTypeWithProperties("GENERAL_PROTOCOL", "", [
	["NAME", 					"General",			"Name",						DataType.MULTILINE_VARCHAR,		None,				"Name"],
	["FOR_WHAT", 				"General",			"For what",					DataType.MULTILINE_VARCHAR,		None,				"For what kind of experimental application/readout this sample is used in the lab"],
	["PROTOCOL_TYPE", 			"General",			"Protocol type",			DataType.CONTROLLEDVOCABULARY,	"PROTOCOL_TYPE",	"Category a protocol belongs"],
	["MATERIALS", 				"Materials",		"Materials",				DataType.MULTILINE_VARCHAR,		None,				"Machines (and relative set up), special labware required for the protocol."],
	["TIME_REQUIREMENT", 		"Method",			"Time requirement",			DataType.MULTILINE_VARCHAR,		None,				"Time required to complete a protocol"],
	["PROCEDURE",				"Method", 			"Procedure",				DataType.MULTILINE_VARCHAR,		None,				"Procedure required by the protocol by points (1,2,3,...)"],
	["PROTOCOL_EVALUATION", 	"Method",			"Protocol evaluation",		DataType.MULTILINE_VARCHAR,		None,				"Parameters and observations to meet the minimal efficiency of the protocol"],
	["SUGGESTIONS", 			"Comments",			"Suggestions",				DataType.MULTILINE_VARCHAR,		None,				"Suggestions for the protocol"],
	["PROTOCOL_MODIFICATIONS", 	"Comments",			"Protocol modifications",	DataType.MULTILINE_VARCHAR,		None,				"Alternative procedures used to make protocol variations"],
	["PUBLICATION", 			"Comments",			"Publication",				DataType.MULTILINE_VARCHAR,		None,				"Publication from where the information was first found OR technical sheet given by the manufacturer"],
	["XMLCOMMENTS", 			"Comments",			"Comments List",			DataType.XML,					None,				"Several comments can be added by different users"]
]);

createSampleTypeWithProperties("PCR_PROTOCOL", "", [
	["NAME", 					"General",			"Name",						DataType.MULTILINE_VARCHAR,		None,				"Name"],
	["FOR_WHAT", 				"General",			"For what",					DataType.MULTILINE_VARCHAR,		None,				"For what kind of experimental application/readout this sample is used in the lab"],
	["TEMPLATE", 				"General",			"Template",					DataType.CONTROLLEDVOCABULARY,	"TEMPLATE",			"Type of template used in the PCR protocol"],
	["REACTION_MIX", 			"Materials",		"Reaction mix",				DataType.MULTILINE_VARCHAR,		None,				"Reaction mix recipe for the PCR"],
	["THERMOCYCLER_PROTOCOL", 	"Method",			"Thermocycler protocol",	DataType.MULTILINE_VARCHAR,		None,				"Thermocycler protocol for PCR"],
	["PROTOCOL_EVALUATION", 	"Method",			"Protocol evaluation",		DataType.MULTILINE_VARCHAR,		None,				"Parameters and observations to meet the minimal efficiency of the protocol"],
	["SUGGESTIONS", 			"Comments",			"Suggestions",				DataType.MULTILINE_VARCHAR,		None,				"Suggestions for the protocol"],
	["PROTOCOL_MODIFICATIONS", 	"Comments",			"Protocol modifications",	DataType.MULTILINE_VARCHAR,		None,				"Alternative procedures used to make protocol variations"],
	["PUBLICATION", 			"Comments",			"Publication",				DataType.MULTILINE_VARCHAR,		None,				"Publication from where the information was first found OR technical sheet given by the manufacturer"],
	["XMLCOMMENTS", 			"Comments",			"Comments List",			DataType.XML,					None,				"Several comments can be added by different users"]
]);

