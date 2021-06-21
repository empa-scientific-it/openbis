from java.lang import String
from org.apache.commons.io import IOUtils

def assertAttachmentContentContains(actualContentStream, expectedContentString):
    if(expectedContentString == None and actualContentStream <> None):
        actualContentString = String(IOUtils.toByteArray(actualContentStream));
        raise Exception('Attachment content should be: None but was: "' + str(actualContentString) + '"')
    if(expectedContentString <> None and actualContentStream == None):
        raise Exception('Attachment content should contain: "' + str(expectedContentString) + '" but was: None')
    if(expectedContentString <> None and actualContentStream <> None):
        actualContentString = String(IOUtils.toByteArray(actualContentStream));
        if(str(expectedContentString) not in str(actualContentString)):
            raise Exception('Attachment content should contain: "' + str(expectedContentString) + '" but was: "' + str(actualContentString) + '"')

def assertAttachmentCount(attachmentList, expectedCount):
    if(expectedCount == 0 and attachmentList <> None):
        raise Exception('Attachment list should be: None but was: ' + str(attachmentList))
    if(expectedCount > 0 and attachmentList == None):
        raise Exception('Attachment list length should be: ' + str(expectedCount) + ' but the list was: None')
    if(expectedCount > 0 and len(attachmentList) <> expectedCount):
        raise Exception('Attachment list length should be: ' + str(expectedCount) + ' but it was: ' + str(len(attachmentList)))

def assertAttachment(attachment, fileName, title, description, version):
    if(attachment.getFileName() <> fileName):
        raise Exception('Attachment file name should be: "' + str(fileName) + '" but was: "' + str(attachment.getFileName()) + '"')
    if(attachment.getTitle() <> title):
        raise Exception('Attachment title should be: "' + str(title) + '" but was: "' + str(attachment.getTitle()) + '"')
    if(attachment.getDescription() <> description):
        raise Exception('Attachment description should be: "' + str(description) + '" but was: "' + str(attachment.getDescription()) + '"')
    if(attachment.getVersion() <> version):
        raise Exception('Attachment version should be: "' + str(version) + '" but was: "' + str(attachment.getVersion()) + '"')

def testProjectWithoutAttachments(transaction):
    project = transaction.getProject("/CISD/DEFAULT");

    attachments = transaction.listAttachments(project)
    assertAttachmentCount(attachments, 0)
    
    content = transaction.getAttachmentContent(project, "not-existing-attachment", None);
    assertAttachmentContentContains(content, None);

def testProjectWithAttachments(transaction):
    project = transaction.getProject("/CISD/NEMO");

    attachments = transaction.listAttachments(project)
    assertAttachmentCount(attachments, 1)
    assertAttachment(attachments[0], "projectDescription.txt", "The Project", "All about it.", 1);

    content = transaction.getAttachmentContent(project, "projectDescription.txt", None);
    assertAttachmentContentContains(content, "3VCP1");
    
    content2 = transaction.getAttachmentContent(project, "not-existing-attachment", None);
    assertAttachmentContentContains(content2, None);

def testExperimentWithoutAttachments(transaction):
    experiment = transaction.getExperiment("/CISD/NEMO/EXP10");

    attachments = transaction.listAttachments(experiment)
    assertAttachmentCount(attachments, 0)

    content = transaction.getAttachmentContent(experiment, "not-existing-attachment", 2);
    assertAttachmentContentContains(content, None);

def testExperimentWithAttachments(transaction):
    experiment = transaction.getExperiment("/CISD/NEMO/EXP1");

    attachments = transaction.listAttachments(experiment)
    assertAttachmentCount(attachments, 4)
    assertAttachment(attachments[0], "exampleExperiments.txt", None, None, 1)
    assertAttachment(attachments[1], "exampleExperiments.txt", None, None, 2)
    assertAttachment(attachments[2], "exampleExperiments.txt", None, "Second latest version", 3)
    assertAttachment(attachments[3], "exampleExperiments.txt", "Latest version", None, 4)
    
    content = transaction.getAttachmentContent(experiment, "exampleExperiments.txt", 2);
    assertAttachmentContentContains(content, "koko");

    content2 = transaction.getAttachmentContent(experiment, "not-existing-attachment", 2);
    assertAttachmentContentContains(content2, None);

def testSampleWithoutAttachments(transaction):
    sample = transaction.getSample("/CISD/NEMO/3VCP5");

    attachments = transaction.listAttachments(sample)
    assertAttachmentCount(attachments, 0)

    content = transaction.getAttachmentContent(sample, "not-existing-attachment", None);
    assertAttachmentContentContains(content, None);

def testSampleWithAttachments(transaction):
    sample = transaction.getSample("/CISD/NEMO/3VCP6");

    attachments = transaction.listAttachments(sample)
    assertAttachmentCount(attachments, 1)
    assertAttachment(attachments[0], "sampleHistory.txt", None, None, 1)

    content = transaction.getAttachmentContent(sample, "sampleHistory.txt", None);
    assertAttachmentContentContains(content, "kot")
    
    content2 = transaction.getAttachmentContent(sample, "not-existing-attachment", None);
    assertAttachmentContentContains(content2, None);

def process(transaction):

    testProjectWithoutAttachments(transaction);
    testProjectWithAttachments(transaction);

    testExperimentWithoutAttachments(transaction);
    testExperimentWithAttachments(transaction);

    testSampleWithoutAttachments(transaction);
    testSampleWithAttachments(transaction);

    if("failure" in transaction.getIncoming().getName()):
        raise Exception('Triggering failure')
