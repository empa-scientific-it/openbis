package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.VersionUtils;

import java.util.List;
import java.util.Map;

public class VocabularyTermImportHelper extends BasicImportHelper
{
    private String vocabularyCode;

    private final Map<String, Integer> versions;

    private final DelayedExecutionDecorator delayedExecutor;

    public VocabularyTermImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, Map<String, Integer> versions)
    {
        super(mode);
        this.versions = versions;
        this.delayedExecutor = delayedExecutor;
    }

    @Override public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        Map<String, Integer> header = parseHeader(page.get(start), false);
        vocabularyCode = getValueByColumnName(header, page.get(start + 1), "Code");

        super.importBlock(page, pageIndex, start + 2, end);
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.VOCABULARY_TERM;
    }

    @Override protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        String termVersion = getValueByColumnName(header, values, "Version");
        String termCode = getValueByColumnName(header, values, "Code");

        return VersionUtils.isNewVersion(termVersion,
                VersionUtils.getStoredVersion(versions, ImportTypes.VOCABULARY_TERM.getType() + "-" + vocabularyCode, termCode));
    }

    @Override protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        String termVersion = getValueByColumnName(header, values, "Version");
        String termCode = getValueByColumnName(header, values, "Code");

        VersionUtils.updateVersion(termVersion, versions, ImportTypes.VOCABULARY_TERM.getType() + "-" + vocabularyCode, termCode);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String termCode = getValueByColumnName(header, values, "Code");
        VocabularyTermPermId termId = new VocabularyTermPermId(termCode, vocabularyCode);

        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.withVocabulary();

        return this.delayedExecutor.getVocabularyTerm(termId, fetchOptions) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String termCode = getValueByColumnName(header, values, "Code");
        String termLabel = getValueByColumnName(header, values, "Label");
        String termDescription = getValueByColumnName(header, values, "Description");

        VocabularyPermId vocabularyPermId = new VocabularyPermId(vocabularyCode);

        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setVocabularyId(vocabularyPermId);
        creation.setCode(termCode);
        creation.setLabel(termLabel);
        creation.setDescription(termDescription);

        this.delayedExecutor.createVocabularyTerm(creation);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String termCode = getValueByColumnName(header, values, "Code");
        String termLabel = getValueByColumnName(header, values, "Label");
        String termDescription = getValueByColumnName(header, values, "Description");

        VocabularyTermPermId termId = new VocabularyTermPermId(termCode, vocabularyCode);

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(termId);
        update.setLabel(termLabel);
        update.setDescription(termDescription);

        this.delayedExecutor.updateVocabularyTerm(update);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        checkKeyExistence(header, "Version");
        checkKeyExistence(header, "Code");
        checkKeyExistence(header, "Label");
        checkKeyExistence(header, "Description");
    }
}
