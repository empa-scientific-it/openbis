#!/usr/bin/python
# Creates an update of the ORF materials for the DynamiX project.
# The output file has to be imported using material batch update.
#
# Before you run this script you have to manually download the list of all ORF codes from openBIS
# 1. Go to:
#      http://dynamix.vital-it.ch/openbis/?viewMode=simple#action=BROWSE&entity=MATERIAL&type=ORF
# 2. Hide all columns instead of "Code" and export the table
# 3. Save the result in 'used-orfs.txt' file and remove the header

# Author: Tomasz Pylak


import os

METADATA_FILE_URL="http://downloads.yeastgenome.org/chromosomal_feature/SGD_features.tab"
HEADER="PRIMARY_SGD_ID	ORF_CLASSIFICATION	code	STANDARD_NAME	ALIAS_NAMES	CHROMOSOME_STANDARD_NAME	DESCRIPTION"
NAME_INDEX=2
ALIAS_INDEX=4

# returns the set of ORFs codes defined in openBIS
def getUsedCodes(filePath):
	f = open(filePath)
	usedCodes = set()
	for line in f:
		line = line.strip()
		if (line[0] == 'Y'):
			usedCodes.add(line)
	return usedCodes
	
def createUpdate(usedCodes, metadataFile, outFileName):
	out = open(outFileName, 'w')
	out.write(HEADER+"\n");
	importedCodes = set()
	
	for line in metadataFile:
		tokens = line.strip().split('\t')
		name = tokens[NAME_INDEX]
		if len(tokens) > ALIAS_INDEX:
			aliases = set(tokens[ALIAS_INDEX].strip().split('|'))
		else: 
			aliases = set()
		if (name in usedCodes):
			out.write(line)
			importedCodes.add(name)
		if len(aliases & usedCodes) > 0:
			used_aliases = aliases & usedCodes
			if len(used_aliases) > 1:
				raise Exception('Alias matches more name: '+(used_aliases))
			alias = used_aliases.pop()
			tokens[NAME_INDEX] = alias
			tokens[ALIAS_INDEX] = tokens[ALIAS_INDEX]+'|'+name
			print "Using alias "+alias+" instead of the systematic name "+name
			out.write('\t'.join(tokens)+"\n")
			importedCodes.add(alias)
			
	out.close()
	
	unimportedCodes = (usedCodes - importedCodes)
	if len(unimportedCodes) > 0:
		print "Missing metadata for: "
		print unimportedCodes


usedCodes = getUsedCodes('used-orfs.txt')

metadataFileName='orf.txt'
metadataFileNameOrg=metadataFileName+'.org'
os.system("wget -O "+metadataFileNameOrg+" "+METADATA_FILE_URL);
os.system("cat "+metadataFileNameOrg+" | cut -f1,3,4,5,6,7,16 > "+metadataFileName);
metadataFile = open(metadataFileName)

createUpdate(usedCodes, metadataFile, 'orf-update.txt')

#os.remove(metadataFileNameOrg)
os.remove(metadataFileName)
