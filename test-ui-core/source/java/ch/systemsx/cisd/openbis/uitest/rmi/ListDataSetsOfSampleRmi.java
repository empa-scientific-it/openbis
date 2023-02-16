/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.rmi.eager.DataSetRmi;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class ListDataSetsOfSampleRmi implements Command<List<DataSet>>
{
    @Inject
    private String session;

    @Inject
    private IGeneralInformationService generalInformationService;

    @Inject
    private ICommonServer commonServer;

    private Sample sample;

    public ListDataSetsOfSampleRmi(Sample sample)
    {
        this.sample = sample;
    }

    @Override
    public List<DataSet> execute()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(
                MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, sample.getCode()));
        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample> searchResult =
                generalInformationService.searchForSamples(
                        session, criteria, EnumSet.allOf(SampleFetchOption.class));
        assert searchResult.size() == 1;

        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dataSets =
                generalInformationService
                        .listDataSetsForSample(session, searchResult.get(0), false);

        List<DataSet> result = new ArrayList<DataSet>();
        for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataSet : dataSets)
        {
            result.add(new DataSetRmi(dataSet, session, commonServer));
        }
        return result;
    }
}
