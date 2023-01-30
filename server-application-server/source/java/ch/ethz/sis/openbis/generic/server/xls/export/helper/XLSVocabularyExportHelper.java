package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.ArrayList;
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
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSVocabularyExportHelper extends AbstractXLSExportHelper
{

    public XLSVocabularyExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber,
            final Map<String, Collection<String>> entityTypeExportPropertiesMap,
            final XLSExport.TextFormatting textFormatting)
    {
        assert permIds.size() == 1;
        final Vocabulary vocabulary = getVocabulary(api, sessionToken, permIds.iterator().next());
        final Collection<String> warnings = new ArrayList<>();

        if (vocabulary != null)
        {
            final String permId = vocabulary.getPermId().getPermId();
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.VOCABULARY, permId, "VOCABULARY_TYPE"));
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.VOCABULARY, permId, "Version", "Code",
                    "Description"));

            warnings.addAll(addRow(rowNumber++, false, ExportableKind.VOCABULARY, permId, "1", vocabulary.getCode(),
                    vocabulary.getDescription()));

            warnings.addAll(addRow(rowNumber++, true, ExportableKind.VOCABULARY, permId, "Version", "Code", "Label",
                    "Description"));

            for (final VocabularyTerm vocabularyTerm : vocabulary.getTerms())
            {
                warnings.addAll(addRow(rowNumber++, false, ExportableKind.VOCABULARY,
                        permId, "1", vocabularyTerm.getCode(),
                        vocabularyTerm.getLabel(), vocabularyTerm.getDescription()));
            }

            return new AdditionResult(rowNumber + 1, warnings);
        } else
        {
            return new AdditionResult(rowNumber, warnings);
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
