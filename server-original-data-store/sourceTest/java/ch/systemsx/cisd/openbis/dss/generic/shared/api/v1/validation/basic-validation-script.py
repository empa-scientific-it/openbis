import os

def validate_data_set_file(file):
	found_match = False
	for filename in os.listdir(file.getAbsolutePath()):
		if 'valid-file.txt' == filename:
			found_match = True
			break
	result = []
	if not found_match:
		result.append(createFileValidationError("No file named valid-file.txt was found in " + file.getName()))
		
	return result

def extract_metadata(file):
	return { "test-prop" : "test-value" }
