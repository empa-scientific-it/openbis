#! /usr/bin/env python

import urllib2
import json
import re

######

def looks_like_number(x):
    try:
        float(x)
        return True
    except ValueError:
        return False

########################################################################

def process_uniprot_accession(accession):
	print "=== Process accession %s ===" % accession
	# 1. Use UniProt accession to get target details
	try:
		target_data = json.loads(urllib2.urlopen("https://www.ebi.ac.uk/chemblws/targets/uniprot/%s.json" % accession).read())
	except urllib2.HTTPError:
		print "No compounds\n"
		return

		
	print "Target Description: %s" % target_data['target']['description']
	print "Target CHEMBLID:    %s" % target_data['target']['chemblId']

	# 2. Get all bioactivties for target CHEMBL_ID
	print "\n"
	bioactivity_data = json.loads(urllib2.urlopen("https://www.ebi.ac.uk/chemblws/targets/%s/bioactivities.json" % target_data['target']['chemblId']).read())
	print "Bioactivity Count:           %d" % len(bioactivity_data['bioactivities'])
	print "Bioactivity Count (IC50's):  %d" % len([record for record in bioactivity_data['bioactivities'] if record['bioactivity_type'] == 'IC50'])

	# 3. Get compounds with high binding affinity (IC50 < 100)
	print "\n"
	for bioactivity in [record for record in bioactivity_data['bioactivities'] if re.search('IC50', record['bioactivity_type']) and looks_like_number(record['value']) and float(record['value']) < 100]:
		print "Compound CHEMBLID: %s" % bioactivity['ingredient_cmpd_chemblid']
#		print json.dumps(bioactivity, sort_keys=True, indent=4) 

# process_uniprot_accession('Q00534') 	# DEBUGGING
with open("uniprot-human-serotonin.tab") as f:
	index = -1
	for line in f:
		index = index + 1		
		if index == 0:
			continue
		cols = line.split("\t")
		process_uniprot_accession(cols[0])