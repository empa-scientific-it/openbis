package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collections;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;

public class XLSVocabularyExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final String permId, int rowNumber)
    {
        final VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms();
        final Map<IVocabularyId, Vocabulary> vocabularies = api.getVocabularies(sessionToken,
                Collections.singletonList(new VocabularyPermId(permId)), fetchOptions);

        assert vocabularies.size() <= 1;

        if (vocabularies.size() > 0)
        {
            final Vocabulary vocabulary = vocabularies.values().iterator().next();

            addRow(wb, rowNumber++, true, "VOCABULARY_TYPE");
            addRow(wb, rowNumber++, true, "Version", "Code", "Description");

            addRow(wb, rowNumber++, false, "1", vocabulary.getCode(), vocabulary.getDescription());

            addRow(wb, rowNumber++, true, "Version", "Code", "Label", "Description");

            for (final VocabularyTerm vocabularyTerm : vocabulary.getTerms())
            {
                addRow(wb, rowNumber++, false, "1", vocabularyTerm.getCode(), vocabularyTerm.getLabel(),
                        vocabularyTerm.getDescription());
            }

            return rowNumber + 1;
        } else
        {
            return rowNumber;
        }

    }

}
