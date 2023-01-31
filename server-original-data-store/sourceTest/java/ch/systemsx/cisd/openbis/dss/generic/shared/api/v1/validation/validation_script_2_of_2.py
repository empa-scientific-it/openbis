def validate_data_set_file(file):
    result = []
    if not find_match(file):
        result.append(createFileValidationError("No file named valid-file.txt was found in " + file.getName()))
        
    return result