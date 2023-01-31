def validate_data_set_file(file):
  errors = []
  if (file.getName().startswith("DssComponentTest-testFailingValidationPutDataSet")):
    errors.append(createFileValidationError(file.getName() + " is not an valid file."))
  return errors