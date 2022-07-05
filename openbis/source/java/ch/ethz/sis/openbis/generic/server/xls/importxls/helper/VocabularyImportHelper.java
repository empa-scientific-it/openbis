package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyUpdate;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.VersionUtils;

import java.util.List;
import java.util.Map;

public class VocabularyImportHelper extends BasicImportHelper
{
    private final DelayedExecutionDecorator delayedExecutor;

    private final Map<String, Integer> versions;

    public VocabularyImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, Map<String, Integer> versions)
    {
        super(mode);
        this.versions = versions;
        this.delayedExecutor = delayedExecutor;
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.VOCABULARY_TYPE;
    }

    @Override protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, "Version");
        String code = getValueByColumnName(header, values, "Code");

        return VersionUtils.isNewVersion(version, VersionUtils.getStoredVersion(versions, ImportTypes.VOCABULARY_TYPE.getType(), code));
    }

    @Override protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, "Version");
        String code = getValueByColumnName(header, values, "Code");

        VersionUtils.updateVersion(version, versions, ImportTypes.VOCABULARY_TYPE.getType(), code);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String vocabularyCode = getValueByColumnName(header, values, "Code");
        VocabularyPermId vocabularyPermId = new VocabularyPermId(vocabularyCode);

        VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms().withVocabulary();
        return delayedExecutor.getVocabulary(vocabularyPermId, fetchOptions) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String vocabularyCode = getValueByColumnName(header, values, "Code");
        String description = getValueByColumnName(header, values, "Description");

        VocabularyCreation create = new VocabularyCreation();
        create.setCode(vocabularyCode);
        create.setManagedInternally(ImportUtils.isInternalNamespace(vocabularyCode));
        create.setDescription(description);
        delayedExecutor.createVocabulary(create);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String vocabularyCode = getValueByColumnName(header, values, "Code");
        String description = getValueByColumnName(header, values, "Description");

        VocabularyPermId vocabularyPermId = new VocabularyPermId(vocabularyCode);

        VocabularyUpdate update = new VocabularyUpdate();
        update.setVocabularyId(vocabularyPermId);
        update.setDescription(description);
        delayedExecutor.updateVocabulary(update);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        checkKeyExistence(header, "Version");
        checkKeyExistence(header, "Code");
        checkKeyExistence(header, "Description");
    }
}
