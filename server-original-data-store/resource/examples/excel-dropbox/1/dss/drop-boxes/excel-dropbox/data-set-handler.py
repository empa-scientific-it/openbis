"""Take an Excel file that matches the template and register the experiments it specifies"""
from ch.systemsx.cisd.openbis.dss.generic.shared.utils import ExcelFileReader

EXPERIMENT_SHEET_NAME = "experiments"

def process(tr):
  """Process the incoming Excel file which contains experiment metadata"""
  incoming = tr.getIncoming()
  workbook = ExcelFileReader.getExcelWorkbook(incoming)
  file_reader = ExcelFileReader(workbook, True)
  lines = file_reader.readLines(EXPERIMENT_SHEET_NAME)

  # Keep track of the spaces and projects we register to avoid duplicate registrations
  space_cache = {}
  project_cache = {}

  # Skip the first line -- it is just the header
  for i in range(1, lines.size()):
    line = lines.get(i)
    register_experiment(tr, line, space_cache, project_cache)

def register_experiment(tr, line, space_cache, project_cache):
  """Register the experiment defined by the line in the Excel file

  The line is expected to follow the following tabular format
  Col 1     Col 2     Col 3             Col 4
  Space     Project   Experiment Code   Description
  """
  space_code = line[0]
  project_code = line[1]
  experiment_code = line[2]
  experiment_desc = line[3]
  space = space_cache.get(space_code)
  if space is None:
    # Create the space and put it in the cache
    space = get_or_register_space(tr, space_code)
    space_cache[space_code] = space

  project_identifier = "/" + space.getSpaceCode() + "/" + project_code
  project = project_cache.get(project_identifier)
  if project is None:
    # Create the project and put it in the cache    
    project = get_or_register_project(tr, project_identifier)
    project_cache[project_identifier] = project

  experiment_identifier = "/" + space_code + "/" + project_code + "/" + experiment_code
  experiment = tr.createNewExperiment(experiment_identifier, "EXCEL_EXAMPLE")
  experiment.setPropertyValue("DESC", experiment_desc)

def get_or_register_space(tr, space_code):
  space = tr.getSpace(space_code)
  if space is None:
    space = tr.createNewSpace(space_code, None)
    space.setDescription("Generated space")
  return space

def get_or_register_project(tr, project_identifier):
  project = tr.getProject(project_identifier)
  if project is None:
    project = tr.createNewProject(project_identifier)
    project.setDescription("Generated project")
  return project





