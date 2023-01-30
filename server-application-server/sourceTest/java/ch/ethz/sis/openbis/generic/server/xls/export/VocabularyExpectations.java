package ch.ethz.sis.openbis.generic.server.xls.export;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class VocabularyExpectations extends Expectations
{

    public VocabularyExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getVocabularies(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        Collections.singletonList(new VocabularyPermId("ANTIBODY.DETECTION")))),
                with(any(VocabularyFetchOptions.class)));

        will(new CustomAction("getting vocabularies")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final VocabularyFetchOptions fetchOptions = (VocabularyFetchOptions) invocation.getParameter(2);

                final Vocabulary vocabulary = new Vocabulary();
                vocabulary.setFetchOptions(fetchOptions);
                vocabulary.setCode("ANTIBODY.DETECTION");
                vocabulary.setDescription("Protein detection system");

                vocabulary.setTerms(getVocabularyTerms(fetchOptions));

                return Collections.singletonMap(new EntityTypePermId("ANTIBODY.DETECTION"), vocabulary);
            }

            private List<VocabularyTerm> getVocabularyTerms(final VocabularyFetchOptions fetchOptions)
            {
                final VocabularyTermFetchOptions vocabularyTermFetchOptions = fetchOptions.withTerms();

                final VocabularyTerm[] vocabularyTerms = new VocabularyTerm[2];

                vocabularyTerms[0] = new VocabularyTerm();
                vocabularyTerms[0].setFetchOptions(vocabularyTermFetchOptions);
                vocabularyTerms[0].setCode("FLUORESCENCE");
                vocabularyTerms[0].setLabel("fluorescent probe");
                vocabularyTerms[0].setDescription("The antibody is conjugated with a fluorescent probe");

                vocabularyTerms[1] = new VocabularyTerm();
                vocabularyTerms[1].setFetchOptions(vocabularyTermFetchOptions);
                vocabularyTerms[1].setCode("HRP");
                vocabularyTerms[1].setLabel("horseradish peroxydase");
                vocabularyTerms[1].setDescription("The antibody is conjugated with the horseradish peroxydase");

                return Arrays.asList(vocabularyTerms);
            }

        });
    }

}
