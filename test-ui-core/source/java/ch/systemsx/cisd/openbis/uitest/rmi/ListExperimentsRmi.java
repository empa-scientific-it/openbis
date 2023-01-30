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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.rmi.eager.ExperimentRmi;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;

/**
 * @author anttil
 */
public class ListExperimentsRmi implements Command<List<Experiment>>
{

    @Inject
    private String session;

    @Inject
    private IGeneralInformationService generalInformationService;

    @Inject
    private ICommonServer commonServer;

    private List<String> ids;

    public ListExperimentsRmi(String firstId, String... rest)
    {
        this.ids = new ArrayList<String>();
        this.ids.add(firstId);
        this.ids.addAll(Arrays.asList(rest));
    }

    @Override
    public List<Experiment> execute()
    {
        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment> experiments =
                generalInformationService.listExperiments(session, ids);

        List<Experiment> result = new ArrayList<Experiment>();
        for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment e : experiments)
        {
            result.add(new ExperimentRmi(e, session, commonServer));
        }
        return result;

    }
}
