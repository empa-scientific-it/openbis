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
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.rmi.eager.ExperimentRmi;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.Project;

/**
 * @author anttil
 */
public class ListExperimentsOfProjectsRmi implements Command<List<Experiment>>
{

    @Inject
    private String session;

    @Inject
    private IGeneralInformationService generalInformationService;

    @Inject
    private ICommonServer commonServer;

    private Collection<Project> projects;

    public ListExperimentsOfProjectsRmi(Project first, Project... rest)
    {
        this.projects = new HashSet<Project>();
        this.projects.add(first);
        this.projects.addAll(Arrays.asList(rest));
    }

    @Override
    public List<Experiment> execute()
    {

        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project> rmiProjects =
                generalInformationService.listProjects(session);

        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project> rmiProjectsReduced =
                new ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project>();
        for (Project project : projects)
        {
            for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project p : rmiProjects)
            {
                if (p.getCode().equalsIgnoreCase(project.getCode()))
                {
                    rmiProjectsReduced.add(p);
                    continue;
                }
            }
        }

        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment> experiments =
                generalInformationService.listExperiments(session, rmiProjectsReduced, null);

        List<Experiment> result = new ArrayList<Experiment>();
        for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment e : experiments)
        {
            result.add(new ExperimentRmi(e, session, commonServer));
        }
        return result;

    }
}
