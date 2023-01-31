from ch.systemsx.cisd.common.mail import EMailAddress
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import DataSetKind

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

def add_attachment(entity, transaction):
    f = open("%s/%s" % (transaction.getIncoming().getPath(), "set1.txt"), 'r')
    entity.addAttachment("attachment.txt", 'Source Import File', 'Source Import File ', f.read())
    f.close()


def create_space(transaction):
    space = transaction.createNewSpace(SPACE_CODE, None)
    space.setDescription("A demo space")

def create_project(transaction):
    project = transaction.createNewProject(PROJECT_ID)
    project.setDescription("A demo project")
    add_attachment(project, transaction)

def create_experiment(transaction):
    exp = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
    exp.setPropertyValue("DESCRIPTION", "A sample experiment")
    add_attachment(exp, transaction)
    return exp

def createMaterials(transaction):
    for x in range(0,60):
        mat = transaction.createNewMaterial("RM_%d" % x, "SLOW_GENE")
        mat.setPropertyValue("GENE_SYMBOL", "RM_%d_S" %x)

def createSamples(transaction):
    sample = transaction.createNewSample('/RICH_SPACE/SAMPLE123', 'DYNAMIC_PLATE')
    add_attachment(sample, transaction)

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
        
def check_get_by_id_methods(transaction):
    ss = transaction.getSearchService()
    if ss.getSample("/CISD/CP-TEST-1") != ss.getSampleByIdentifier("/CISD/CP-TEST-1"):
        raise Exception("Method - getSample and getSampleByIdentifier returned different sample")
    
    if ss.getProject("/CISD/NEMO") != ss.getProjectByIdentifier("/CISD/NEMO"):
        raise Exception("Method - getProject and getProjectByIdentifier returned different sample")
        
    if ss.getExperiment("/CISD/NEMO/EXP1") != ss.getExperimentByIdentifier("/CISD/NEMO/EXP1"):
        raise Exception("Method - getExperiment and getExperimentByIdentifier returned different sample")

def search_unique_sample(transaction, sample_code): 
    search_service = transaction.getSearchService()
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sample_code));
    found_sample = search_service.searchForSamples(sc)
    if found_sample.size() != 1:
      raise Exception("Unique sample %s not found with search criteria" % sample_code)
    print "unique sample found"
    print found_sample
    print "----------------------------------------"

def process(transaction):
    #reading methods
    check_get_by_id_methods(transaction)
    
    #search for unique sample using search criteria
    search_unique_sample(transaction, "CP-TEST-3")
    
    # create experiment
    create_space(transaction)
    create_project(transaction)
    experiment = create_experiment(transaction)
    
    # register link data set
    link = transaction.createNewDataSet("LINK_TYPE", "FR_LINK_CODE")
    link.setDataSetKind(DataSetKind.LINK);
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
