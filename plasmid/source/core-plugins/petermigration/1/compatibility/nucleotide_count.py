import re

def calculate_Onuc(): 
	result = len(re.findall("R|r|Y|y|M|m|K|k|S|s|W|w|H|h|b|B|D|d|X|x|n|N", entity.propertyValue('SEQUENCE')))	

	return result
     
def calculate():
	return calculate_Onuc()
