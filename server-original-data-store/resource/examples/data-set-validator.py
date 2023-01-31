import os

def validate_data_set_file(file):

    errors = []

    if file.isFile():
        errors.append(createFileValidationError("'" + file.getPath() + "' is a normal file. Expected a directory."))
    else:
        for f in file.listFiles():
            if f.getName().startswith('file'):
                pf = open(f.getPath(), "r")
                if pf.read().find('invalid') > -1:
                    errors.append(createFileValidationError("File '" + f.getPath() + "' contains invalid text."))
            else:
                errors.append(createFileValidationError("File '" + f.getPath() + "' doesn't match file name pattern 'file*'."))

    return errors