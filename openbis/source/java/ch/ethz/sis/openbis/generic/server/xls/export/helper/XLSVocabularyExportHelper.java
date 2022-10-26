package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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
            final Collection<String> permIds, int rowNumber)
    {
        assert permIds.size() == 1;
        final Vocabulary vocabulary = getVocabulary(api, sessionToken, permIds.iterator().next());

        if (vocabulary != null)
        {

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

    private Vocabulary getVocabulary(final IApplicationServerApi api, final String sessionToken, final String permId)
    {
        final VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms();
        final Map<IVocabularyId, Vocabulary> vocabularies = api.getVocabularies(sessionToken,
                Collections.singletonList(new VocabularyPermId(permId)), fetchOptions);

        assert vocabularies.size() <= 1;

        final Iterator<Vocabulary> iterator = vocabularies.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

}
