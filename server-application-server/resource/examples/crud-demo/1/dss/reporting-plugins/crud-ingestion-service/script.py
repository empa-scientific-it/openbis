from java.lang import IllegalArgumentException
    
def insert(query, info):
  """Insert an entry into the table"""
  stmt = "INSERT INTO exampledata (info) VALUES (?1)"
  query.insert(stmt, [info])
  
  
def update(query, info, identifier):
  """Update an existing entry from the table"""
  stmt = "UPDATE exampledata SET info=?1 WHERE id=?2"
  query.update(stmt, [info, identifier])
  
def delete(query, identifier):
  """Delete an entry from the table"""
  stmt = "DELETE FROM exampledata WHERE id=?1"
  query.update(stmt, [identifier])

def register_data_set(tr):
  # The identifiers of various entities
  spaceId = "CRUD"
  projectId = "/CRUD/DATA"
  experimentId = "/CRUD/DATA/DATA"
  space = tr.getSpace(spaceId)
  if space is None:
    space = tr.createNewSpace(spaceId, None)

  project = tr.getProject(projectId)
  if project is None:
    project = tr.createNewProject(projectId)

  exp = tr.getExperiment(experimentId)
  if exp is None:
    # Make it an HCS experiment because that is available by default.
    exp = tr.createNewExperiment(experimentId, "COMPOUND_HCS")
    exp.setPropertyValue("DESCRIPTION", "An experiment for storing data sent to a secondary database.")

  ds = tr.createNewDataSet()
  ds.setExperiment(exp)
  tr.moveFile(tr.getIncoming().getPath(), ds)

def process(tr, parameters, tableBuilder):
  """Add, modify, or delete a row from the table, depending on the request parameters.
  
  One parameter is required: 'operation':
    - operation: must be one of 'INSERT', 'DELETE', or 'UPDATE'
  
  For the operations 'INSERT' and 'UPDATE' another parameter is required:
    - info: the content of the info column in the row.
  
  If the opration is 'DELETE' or 'UPDATE', one additional parameter is required:
    - id : the identifier of the row to modify.
  """
  operation = parameters.get('operation')
  if operation is None:
    # This error message should be more precise, but this is just an example...
    raise IllegalArgumentException("The 'operation' parameter must be specified")

  if 'INSERT' == operation or 'UPDATE' == operation:
    info = parameters.get('info')
    if info is None:
      raise IllegalArgumentException("The 'info' parameter must be specified")
  if 'DELETE' == operation or 'UPDATE' == operation:
    identifier = parameters.get('id')
    if identifier is None:
      raise IllegalArgumentException("The 'id' parameter must be specified for DELETE and UPDATE")

  query = tr.getDatabaseQuery("crud-db")
  # Dispatch to the appropriate operation
  if 'INSERT' == operation:
    insert(query, info)
  elif 'DELETE' == operation:
    delete(query, identifier)
  elif 'UPDATE' == operation:
    update(query, info, identifier)
  else:
    raise IllegalArgumentException("The operation must be one of 'INSERT','DELETE', or 'UPDATE")

  register_data_set(tr)

  PARMS_HEADER = "Parameters"
  tableBuilder.addHeader(PARMS_HEADER)
  row = tableBuilder.addRow()
  row.setCell(PARMS_HEADER, str(parameters))
