from ch.systemsx.cisd.common.mail import EMailAddress

SPACE_CODE = "RICH_SPACE"
PROJECT_ID = "/RICH_SPACE/RICH_PROJECT"
EXPERIMENT_ID = "/RICH_SPACE/RICH_PROJECT/RICH_EXPERIMENT"

# the hooks

def sendMail(context, subject, message):
    mailClient = context.getGlobalState().getMailClient();
    addressFrom = EMailAddress("example@example.com")
    addressTo = EMailAddress("rich_test_example@example.com", "example name")
    mailClient.sendEmailMessage(subject, message, None,
            addressFrom, addressTo) 

def post_metadata_registration(context):
    content = "post_metadata_registration rich %s " % context.getPersistentMap().get("email_text")
    sendMail(context, "Subject", content)

def create_space_if_needed(transaction):
    space = transaction.getSpace(SPACE_CODE)
    if None == space:
        space = transaction.createNewSpace(SPACE_CODE, None)
        space.setDescription("A demo space")

def create_project_if_needed(transaction):
    project = transaction.getProject(PROJECT_ID)
    if None == project:
        create_space_if_needed(transaction)
        project = transaction.createNewProject(PROJECT_ID)
        project.setDescription("A demo project")
        f = open("%s/%s" % (transaction.getIncoming().getPath(), "set1.txt"), 'r')
        project.addAttachment("attachment.txt", 'Source Import File', 'Source Import File ', f.read())
        f.close()

def create_experiment_if_needed(transaction):
    exp = transaction.getExperiment(EXPERIMENT_ID)
    if None == exp:
        create_project_if_needed(transaction)
        exp = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
        exp.setPropertyValue("DESCRIPTION", "A sample experiment")

    return exp

def createMaterials(transaction):
    for x in range(0,60):
        mat = transaction.createNewMaterial("RM_%d" % x, "SLOW_GENE")
        mat.setPropertyValue("GENE_SYMBOL", "RM_%d_S" %x)

def createSamples(transaction):
    sample = transaction.createNewSample('/RICH_SPACE/SAMPLE123', 'DYNAMIC_PLATE')

def updateMaterial(transaction):
    ma = transaction.getMaterialForUpdate("AD3", "VIRUS");
    ma.setPropertyValue("DESCRIPTION", "modified description");

def updateExperiment(transaction):
    ex = transaction.getExperimentForUpdate("/CISD/NEMO/EXP1")
    ex.setPropertyValue("DESCRIPTION", "modified experiment description")

def createBacterias(transaction):
    vocabulary = transaction.getSearchService().searchForVocabulary("ORGANISM")
    for term in vocabulary.getTerms():
        mat = transaction.createNewMaterial("BC_%s" % term.getCode(), "BACTERIUM")
        mat.setPropertyValue("DESCRIPTION", term.getDescription())
        mat.setPropertyValue("ORGANISM", term.getCode())
        
def process(transaction):
    # create experiment
    experiment = create_experiment_if_needed(transaction)
    
    # register link data set
    link = transaction.createNewDataSet("LINK_TYPE", "FR_LINK_CODE")
    link.setExperiment(experiment)
    link.setExternalCode("EX_CODE")
    link.setExternalDataManagementSystem(transaction.getExternalDataManagementSystem("DMS_1"))

    # register many materials
    createMaterials(transaction)
    
    # update material
    updateMaterial(transaction) 

    # update existing experiment
    updateExperiment(transaction)

    # register samples
    createSamples(transaction)
    
    # read controlled vocabularies and create materials
    createBacterias(transaction)
    
    transaction.getRegistrationContext().getPersistentMap().put("email_text", "rich_email_text")
