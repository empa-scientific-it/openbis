import os, glob, re, csv, time, shutil, sys
from time import *
from datetime import *

incoming =sys.argv[1]



#The siRNA csv files are renamed to use the the same well names as in openBIS, instead of w1, w2, etc
 
def get_sirna_wells(incoming):
	for csvfile in glob.glob(os.path.join(incoming, 'w*_sirna.csv')):
		(dirName, fileName) = os.path.split(csvfile)
		(basename, extension) = os.path.splitext(fileName)
		token_list = re.split(r"[_]",basename)
		c_well = token_list[0]
		other_well = token_list[1]
		control_well=''
		well=''
		if (c_well == "w1"):
			control_well = "A1"	
		if (c_well == "w2"):
			control_well = "A2"
		if (other_well == "w3"):
			well = "A3"
		if (other_well == "w4"):
			well = "A4"
		if (other_well == "w5"):
			well = "A5"
		if (other_well == "w6"):
			well = "A6"
		if (other_well == "w7"):
			well = "B1"
		if (other_well == "w8"):
			well = "B2"
		if (other_well == "w9"):
			well = "B3"
		if (other_well == "w10"):
			well = "B4"
		if (other_well == "w11"):
			well = "B5"
		if (other_well == "w12"):
			well = "B6"
		if (other_well == "w13"):
			well = "C1"
		if (other_well == "w14"):
			well = "C2"
		if (other_well == "w15"):
			well = "C3"
		if (other_well == "w16"):
			well = "C4"
		if (other_well == "w17"):
			well = "C5"
		if (other_well == "w18"):
			well = "C6"
		if (other_well == "w19"):
			well = "D1"
		if (other_well == "w20"):
			well = "D2"
		if (other_well == "w21"):
			well = "D3"
		if (other_well == "w22"):
			well = "D4"
		if (other_well == "w23"):
			well = "D5"
		if (other_well == "w24"):
			well = "D6"
		
		new_csv = incoming + "/" + control_well + "_" + well + "_sirna.csv"
				
		if not os.path.exists(new_csv): 
			shutil.move(csvfile, new_csv) 

get_sirna_wells(incoming)


#The gene csv files are renamed to use the same well names as in openBIS, instead of w1, w2, etc

def get_gene_wells(incoming):
	for csvfile in glob.glob(os.path.join(incoming, 'w*_gene.csv')):
		(dirName, fileName) = os.path.split(csvfile)
		(basename, extension) = os.path.splitext(fileName)
		token_list = re.split(r"[_]",basename)
		c_well = token_list[0]
		other_well = token_list[1]
		control_well=''
		gene_well=''
		if (c_well == "w1"):
			control_well = "A1"	
		if (c_well == "w2"):
			control_well = "A2"
		if (other_well == "w4-w5-w6"):
			gene_well = "A4-A5-A6"
		if (other_well == "w7-w8-w9"):
			gene_well = "B1-B2-B3"
		if (other_well == "w10-w11-w12"):
			gene_well = "B4-B5-B6"
		if (other_well == "w13-w14-w15"):
			gene_well = "C1-C2-C3"
		if (other_well == "w16-w17-w18"):
			gene_well = "C4-C5-C6"
		if (other_well == "w19-w20-w21"):
			gene_well = "D1-D2-D3"
		if (other_well == "w22-w23-w24"):
			gene_well = "D4-D5-D6"
		
		gene_csv = incoming + "/" + control_well + "_" + gene_well + "_gene.csv"
				
		if not os.path.exists(gene_csv):
			shutil.move(csvfile, gene_csv)

get_gene_wells(incoming)


# The plate code is extracted from the file OriginalDataDirectory.txt. This is the plate that contains the images produced by Ludovico and the matlab files given by Fethallah
def extractPlateCode(incoming):
	plateCode = ''
	for textfile in glob.glob(os.path.join(incoming, 'OriginalDataDirectory.txt')):
	  text = open(textfile, "r")
	  lineIndex =0
	  for line in text:
		lineIndex=lineIndex+1
		if re.match('PLATE',line):
			token_list = re.split(r"[\t]",line)
		  	partialCode = token_list[0]
		  	plateCode = partialCode[0:9]
	
	return plateCode    
	  
extractPlateCode(incoming)

#The file  Info_plates_sirna_genes.txt contains info on what genes and siRNA are contained in each well of each plate. If the plate code extracted above is the same as one of the plate codes in the file, the info regarding that plate is extracted 
def extractInfoPlates(incoming):
	well_list=[]
	plate_list=[]
	sirna_list=[]
	gene_list=[]
	for textfile in glob.glob(os.path.join(incoming, 'Info_plates_sirna_genes.txt')):
	  text = open(textfile, "r")
	  lineIndex =0
	  for line in text:
		lineIndex=lineIndex+1
		token_list = re.split(r"[\t]",line)
		token_list = [ item.strip() for item in token_list ]
		token_list = filter(lambda x: len(x) > 0, token_list)
		well = token_list[0]
		plate = token_list[1]
		sirna = token_list[2]
		gene = token_list[3]
		
		if (plate ==  extractPlateCode(incoming).strip()):
			well_list.append(well)
			plate_list.append(plate)
			sirna_list.append(sirna)
			gene_list.append(gene)
	
	return well_list, plate_list, sirna_list, gene_list
	

extractInfoPlates(incoming)  

#The single sirna csv files are combined into one global_siRNA.csv which contains also info on sirna and genes contained in each well
#The single gene csv files are combined into one global_gene.csv which contains also info on genes contained in each well

def parse_csv(incoming):
	global_sirna_csv = incoming+"/global_siRNA.csv"
	global_gene_csv = incoming+"/global_gene.csv"
	f = open(global_sirna_csv, "a")
	g = open(global_gene_csv, "a")

	for csv_file in glob.glob(os.path.join(incoming, 'A*.csv')):
		(dirName2, fileName2) = os.path.split(csv_file)
		(basename2, extension2) = os.path.splitext(fileName2)
		well_list = re.split(r"[_]",basename2)
		control = well_list[0]
		measure = well_list[1]
		meas = measure[0:2]
		csvfile = open(csv_file, "rb")
		test = csv.reader(csvfile, delimiter=',', quotechar='"')


		if (measure == "A3"):
			for x, row in enumerate(test):
				#if (x==0 and control == "A2"):
				#	s = "siRNA Well,"+ "Control Well," + "Gene," + "siRNA," + ','.join(row) +'\n'
				#	f.write(s)
				if x !=0:
					t =  measure + "," + control + "," + "control gene," + "control siRNA" + "," + ",".join(row) +"\n"
					f.write(t)
					



 		for i,j,k in zip(extractInfoPlates(incoming)[0],extractInfoPlates(incoming)[2],extractInfoPlates(incoming)[3]):
			if (measure == i):
				for x, row in enumerate(test):
					if (x==0 and measure == 'A6' and control == 'A2'):
						s = "siRNA Well,"+ "Control Well," + "Gene," + "siRNA," + ','.join(row) +'\n'
						f.write(s)
					if x !=0:
						t =  measure + "," + control + "," + k + "," + j + "," + ",".join(row) + "\n"
						f.write(t)
						f.close


 		for l,m in zip(extractInfoPlates(incoming)[0],extractInfoPlates(incoming)[3]):
			if (meas == l):
				for y, row in enumerate(test):
					print "y, meas, l ", y, meas, control 
					if (y==0 and meas == "C1" and control == "A1"):
						s = "siRNA Well,"+ "Control Well," + "Gene," + ','.join(row) +'\n'
					#	print s
						g.write(s)
					if y !=0:
						t =  measure + "," + control + "," + m + "," +  ",".join(row) + "\n"
						g.write(t)
						g.close


				
parse_csv(incoming)

