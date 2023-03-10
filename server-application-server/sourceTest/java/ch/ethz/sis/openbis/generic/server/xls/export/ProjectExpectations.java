/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class ProjectExpectations extends Expectations
{

    public ProjectExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getProjects(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        List.of(new ProjectPermId("200001010000000-0001"),
                                new ProjectPermId("200001010000000-0002")))),
                with(any(ProjectFetchOptions.class)));

        will(new CustomAction("getting projects")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final ProjectFetchOptions fetchOptions = (ProjectFetchOptions) invocation.getParameter(2);
                fetchOptions.withRegistrator();
                fetchOptions.withModifier();

                final Space[] spaces = new Space[2];

                spaces[0] = new Space();
                spaces[0].setCode("ELN_SETTINGS");

                spaces[1] = new Space();
                spaces[1].setCode("DEFAULT");

                final Calendar calendar = Calendar.getInstance();
                calendar.set(2023, Calendar.MARCH, 10, 17, 23, 44);
                final Date registrationDate = calendar.getTime();

                calendar.set(2023, Calendar.MARCH, 11, 17, 23, 44);
                final Date modificationDate = calendar.getTime();

                final Person registrator = new Person();
                registrator.setUserId("system");

                final Person modifier = new Person();
                modifier.setUserId("test");

                final Project[] projects = new Project[2];

                projects[0] = new Project();
                projects[0].setFetchOptions(fetchOptions);
                projects[0].setPermId(new ProjectPermId("200001010000000-0001"));
                projects[0].setIdentifier(new ProjectIdentifier("/ELN_SETTINGS/STORAGES"));
                projects[0].setCode("STORAGES");
                projects[0].setDescription("Storages");
                projects[0].setSpace(spaces[0]);
                projects[0].setRegistrator(registrator);
                projects[0].setModifier(modifier);
                projects[0].setRegistrationDate(registrationDate);
                projects[0].setModificationDate(modificationDate);

                projects[1] = new Project();
                projects[1].setFetchOptions(fetchOptions);
                projects[1].setPermId(new ProjectPermId("200001010000000-0002"));
                projects[1].setIdentifier(new ProjectIdentifier("/DEFAULT/DEFAULT"));
                projects[1].setCode("DEFAULT");
                projects[1].setDescription("Default");
                projects[1].setSpace(spaces[1]);
                projects[1].setRegistrator(registrator);
                projects[1].setModifier(modifier);
                projects[1].setRegistrationDate(registrationDate);
                projects[1].setModificationDate(modificationDate);

                return Arrays.stream(projects).collect(Collectors.toMap(Project::getIdentifier, Function.identity(),
                        (project1, project2) -> project2, LinkedHashMap::new));
            }

        });
    }

}
