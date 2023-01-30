/*
 * Copyright 2012 ETH Zuerich, CISD
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
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.rmi.eager.DataSetRmi;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class ListDataSetsOfSamplesOnBehalfOfUserRmi implements Command<List<DataSet>>
{
    @Inject
    private String session;

    @Inject
    private IGeneralInformationService generalInformationService;

    @Inject
    private ICommonServer commonServer;

    private User user;

    private Collection<Sample> samples;

    public ListDataSetsOfSamplesOnBehalfOfUserRmi(User user, Sample first, Sample... rest)
    {
        this.user = user;
        this.samples = new ArrayList<Sample>();
        this.samples.add(first);
        this.samples.addAll(Arrays.asList(rest));
    }

    @Override
    public List<DataSet> execute()
    {

        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample> rmiSamples =
                new ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample>();

        for (Sample sample : samples)
        {

            SearchCriteria criteria = new SearchCriteria();
            criteria.addMatchClause(
                    MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, sample.getCode()));
            List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample> searchResult =
                    generalInformationService.searchForSamples(
                            session, criteria, EnumSet.allOf(SampleFetchOption.class));

            assert searchResult.size() == 1;
            rmiSamples.addAll(searchResult);
        }

        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dataSets =
                generalInformationService.listDataSetsOnBehalfOfUser(session, rmiSamples, EnumSet
                        .allOf(Connections.class), user.getName());

        List<DataSet> result = new ArrayList<DataSet>();
        for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataSet : dataSets)
        {
            result.add(new DataSetRmi(dataSet, session, commonServer));
        }
        return result;
    }
}
