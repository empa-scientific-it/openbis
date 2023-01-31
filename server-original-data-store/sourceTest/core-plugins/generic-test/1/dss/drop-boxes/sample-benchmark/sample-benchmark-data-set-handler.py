from time import time
from ch.systemsx.cisd.common.mail import EMailAddress

# modify those values to test the performance. 
# sample type can be important, as it can contain validations for instance.
# The values present here are not too high, so that the test runs quickly in normal circumstances.

SAMPLES_COUNT = 50
SAMPLE_TYPE = 'WELL'

SPACE_CODE = "SAMPLE_BENCHMARK_SPACE"
PROJECT_ID = "/SAMPLE_BENCHMARK_SPACE/SAMPLE_BENCHMARK_PROJECT"
EXPERIMENT_ID = "/SAMPLE_BENCHMARK_SPACE/SAMPLE_BENCHMARK_PROJECT/SAMPLE_BENCHMARK_EXPERIMENT"

# the hooks

def sendMail(context, subject, message):
    mailClient = context.getGlobalState().getMailClient();
    addressFrom = EMailAddress("example@example.com")
    addressTo = EMailAddress("example@example.com", "example name")
    mailClient.sendEmailMessage(subject, message, None,
            addressFrom, addressTo) 

def pre_metadata_registration(context):
    context.getPersistentMap().put("pre_registration", time())

def post_metadata_registration(context):
    start = context.getPersistentMap().get("start")
    pre = context.getPersistentMap().get("pre_registration")
    post = time()
    
    contentPattern = "sample_benchmark_test\nTime in seconds\n  jython script:   %s\n  as registration: %s\n  total            %s"
    content =  contentPattern % (str(pre-start), str(post-pre), str(post - start))
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

def create_experiment_if_needed(transaction):
    exp = transaction.getExperiment(EXPERIMENT_ID)
    if None == exp:
        create_project_if_needed(transaction)
        exp = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
        exp.setPropertyValue("DESCRIPTION", "A sample experiment")

    return exp

def createSamples(transaction):
    sampleParent = transaction.createNewSample('/'+SPACE_CODE+'/SAMPLE_PARENT', SAMPLE_TYPE)
    for i in range(SAMPLES_COUNT):
        sample = transaction.createNewSample('/'+SPACE_CODE+'/SAMPLE'+str(i), SAMPLE_TYPE)
        sample.setParentSampleIdentifiers([sampleParent.getSampleIdentifier()])

def process(transaction):
    transaction.getRegistrationContext().getPersistentMap().put("start", time())
    
    # create experiment
    experiment = create_experiment_if_needed(transaction)
    
    # register samples
    createSamples(transaction)
