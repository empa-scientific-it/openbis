/*
 * Copyright 2013 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IVocabularyTermImmutable;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;

/**
 * @author Manuel Kohler
 */
public class AlignmentJavaDataSetRegistrationDropboxV2 extends
        AbstractJavaDataSetRegistrationDropboxV2
{

    private final static String DATA_SET_TYPE_ALIGNMENT = "ALIGNMENT";

    public List<IDataSetImmutable> searchDs(IDataSetRegistrationTransactionV2 transaction, String dscode)
    {
        // search for the data set
        ISearchService search_service = transaction.getSearchService();
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, dscode));
        List<IDataSetImmutable> myds = search_service.searchForDataSets(sc);
        assert (myds.size() == 1);

        return myds;
    }

    public void addVocabularyTerm(IDataSetRegistrationTransactionV2 transaction, String vocabularyCode)
    {
        IVocabulary modifiableVocabulary = transaction.getVocabularyForUpdate(vocabularyCode);
        if (!modifiableVocabulary.containsTerm(vocabularyCode))
        {
            ArrayList<Long> ordinals = new ArrayList<Long>();

            List<IVocabularyTermImmutable> terms = modifiableVocabulary.getTerms();
            for (IVocabularyTermImmutable term : terms)
            {
                ordinals.add(term.getOrdinal());
            }
            // max(ordinals);

            IVocabularyTerm newTerm = transaction.createNewVocabularyTerm();
            newTerm.setCode(vocabularyCode);
            newTerm.setOrdinal(1L);
            modifiableVocabulary.addTerm(newTerm);
        }

    }

    @Override
    public void process(IDataSetRegistrationTransactionV2 transaction)
    {
        String dsCode = transaction.getIncoming().getName();
        List<IDataSetImmutable> existingDs = searchDs(transaction, dsCode);
        IDataSetImmutable firstDs = existingDs.get(0);

        String sampleId = firstDs.getSample().getSampleIdentifier();
        ISearchService search_service = transaction.getSearchService();
        ISampleImmutable s = search_service.getSample(sampleId);

        String prepared = s.getPropertyValue("PREPARED_BY");

        // transaction.getGlobalState().getMailClient().sendEmailMessage(subject, content, replyToOrNull, fromOrNull, recipients)

        IDataSet newDataSet = transaction.createNewDataSet(DATA_SET_TYPE_ALIGNMENT);
        newDataSet.setDataSetKind(DataSetKind.PHYSICAL);

        IExperimentImmutable experiment = firstDs.getExperiment();

        ArrayList<String> tmpList = new ArrayList<String>();
        tmpList.add(firstDs.getDataSetCode());
        newDataSet.setParentDatasets(tmpList);
        newDataSet.setExperiment(experiment);
        transaction.moveFile(transaction.getIncoming().getAbsolutePath(), newDataSet);
    }
}
