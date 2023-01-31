
def create_project_and_experiment(transaction, space_id, project_id, experiment_id):
    space = transaction.createNewSpace(space_id, None)
    space.setDescription("A demo space")
    project = transaction.createNewProject(project_id)
    project.setDescription("A demo project")
    exp = transaction.createNewExperiment(experiment_id, 'SIRNA_HCS')
    exp.setPropertyValue("DESCRIPTION", "A sample experiment")
    return exp
    
def process(transaction, parameters, tableBuilder):
  space_id = parameters.get("space_id")
  project_id = "/%s/META" % space_id
  experiment_id = "%s/META" % project_id

  experiment = create_project_and_experiment(transaction, space_id, project_id, experiment_id)

  existing_metaproject = transaction.getMetaproject("TEST_METAPROJECTS", "test")
  copy_metaproject = transaction.createNewMetaproject("%s_COPY_TEST_METAPROJCTS" % space_id, existing_metaproject.getDescription(), "test")
  
  new_metaproject = transaction.createNewMetaproject("%s_TEST_META" % space_id, "description", "test")
  
  sample = transaction.createNewSample("/%s/SAMPLE" % (space_id), "NORMAL")
  new_metaproject.addEntity(sample)
  new_metaproject.addEntity(experiment)


  tableBuilder.addHeader("VALUE")  

  def addRow(value):
    row = tableBuilder.addRow()
    row.setCell("VALUE", value)

  transaction.setUserId("test")
  ms = transaction.getSearchService().listMetaprojects()
  for m in ms:
    if (not m.getName().startswith("META")):
      addRow("%s %s %s" % (m.getName(), m.getDescription(), m.getOwnerId()))
  
  ms = transaction.getSearchServiceFilteredForUser("test_role").listMetaprojects()
  for m in ms:
    addRow("%s %s %s" % (m.getName(), m.getDescription(), m.getOwnerId()))
     
  mas = transaction.getSearchServiceFilteredForUser("test_role").getMetaprojectAssignments("TEST_METAPROJECTS")
  addRow("ASSIGNMENTS")
  m = mas.getMetaproject()
  ("%s %s %s" % (m.getName(), m.getDescription(), m.getOwnerId()))
  for s in mas.getSamples():
    addRow("SAMPLE %s %s" % (s.getCode(), s.getPermId()))
  for e in mas.getExperiments():
    addRow("EXPERIMENT %s %s" % (e.getExperimentIdentifier(), e.getPermId()))
  for d in mas.getDataSets():
    addRow("DATASET %s" % d.getDataSetCode())
  for m in mas.getMaterials():
    addRow("MATERIAL %s" % m.getMaterialIdentifier())
  
  oldSample = transaction.getSample("/TEST-SPACE/TEST-PROJECT/EV-TEST")
  
  ms = transaction.getSearchServiceFilteredForUser("test_role").listMetaprojectsForEntity(oldSample)
  for m in ms:
    addRow("OLD-SAMPLE %s %s %s" % (m.getName(), m.getDescription(), m.getOwnerId()))
  
  