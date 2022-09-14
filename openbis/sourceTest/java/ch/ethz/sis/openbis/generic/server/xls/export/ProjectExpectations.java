package ch.ethz.sis.openbis.generic.server.xls.export;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
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

                final Space[] spaces = new Space[2];

                spaces[0] = new Space();
                spaces[0].setCode("ELN_SETTINGS");

                spaces[1] = new Space();
                spaces[1].setCode("DEFAULT");

                final Project[] projects = new Project[2];

                projects[0] = new Project();
                projects[0].setFetchOptions(fetchOptions);
                projects[0].setPermId(new ProjectPermId("200001010000000-0001"));
                projects[0].setIdentifier(new ProjectIdentifier("/ELN_SETTINGS/STORAGES"));
                projects[0].setCode("STORAGES");
                projects[0].setDescription("Storages");
                projects[0].setSpace(spaces[0]);

                projects[1] = new Project();
                projects[1].setFetchOptions(fetchOptions);
                projects[1].setPermId(new ProjectPermId("200001010000000-0002"));
                projects[1].setIdentifier(new ProjectIdentifier("/DEFAULT/DEFAULT"));
                projects[1].setCode("DEFAULT");
                projects[1].setDescription("Default");
                projects[1].setSpace(spaces[1]);

                return Arrays.stream(projects).collect(Collectors.toMap(Project::getIdentifier, Function.identity(),
                        (project1, project2) -> project2, LinkedHashMap::new));
            }

        });
    }

}
