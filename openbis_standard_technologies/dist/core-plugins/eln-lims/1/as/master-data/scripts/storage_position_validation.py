from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider
from ch.ethz.sis.openbis.generic.asapi.v3.dto.service import CustomASServiceExecutionOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id import CustomASServiceCode;

def validate(entity, isNew):
    v3 = CommonServiceProvider.getApplicationServerApi();
    sessionToken = v3.loginAsSystem();
    id = CustomASServiceCode("as-eln-lims-api");
    properties = {};
    for samplePropertyPE in entity.samplePE().getProperties():
        key = samplePropertyPE.getEntityTypePropertyType().getPropertyType().getCode();
        value = None;
        vocabularyTerm = samplePropertyPE.getVocabularyTerm();
        if vocabularyTerm is not None:
            value = vocabularyTerm.getCode();
        else:
            value = samplePropertyPE.getValue();
        properties[key] = value;

    options = CustomASServiceExecutionOptions() \
        .withParameter("method", "isValidStoragePositionToInsertUpdate") \
        .withParameter("samplePermId", entity.samplePE().getPermId()) \
        .withParameter("sampleCode", entity.samplePE().getCode()) \
        .withParameter("sampleProperties", properties);
    v3.executeCustomASService(sessionToken, id, options);
    v3.logout(sessionToken);