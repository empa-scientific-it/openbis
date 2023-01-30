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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.rmi.eager.SampleRmi;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class ListSamplesOfExperimentOnBehalfOfUserRmi implements Command<List<Sample>>
{
    @Inject
    private String session;

    @Inject
    private IGeneralInformationService generalInformationService;

    @Inject
    private ICommonServer commonServer;

    private Experiment experiment;

    private User user;

    public ListSamplesOfExperimentOnBehalfOfUserRmi(Experiment experiment, User user)
    {
        this.experiment = experiment;
        this.user = user;
    }

    @Override
    public List<Sample> execute()
    {
        List<Sample> result = new ArrayList<Sample>();
        for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample s : generalInformationService
                .listSamplesForExperimentOnBehalfOfUser(session, Identifiers.get(experiment)
                        .toString(), user.getName()))
        {
            result.add(new SampleRmi(s, session, commonServer));
        }
        return result;
    }
}
