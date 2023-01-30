/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnsupportedObjectIdException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.AbstractExecutorTest;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.IMapProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.MapProjectByIdExecutor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class MapProjectByIdExecutorTest extends AbstractExecutorTest
{

    private IProjectDAO projectDao;

    private IProjectAuthorizationExecutor authorizationExecutor;

    @Override
    protected void init()
    {
        projectDao = context.mock(IProjectDAO.class);
        authorizationExecutor = context.mock(IProjectAuthorizationExecutor.class);
    }

    @Test
    public void testGetByPermId()
    {
        final ProjectPE project = new ProjectPE();
        project.setId(123L);
        project.setPermId("TEST_PERM_ID");
        final ProjectPermId permId = new ProjectPermId(project.getPermId());

        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);

                    one(projectDao).listByPermID(Collections.singletonList(project.getPermId()));
                    will(returnValue(Collections.singletonList(project)));
                }
            });

        Map<IProjectId, ProjectPE> result = execute(permId);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Long.valueOf(123L), result.get(permId).getId());
    }

    @Test
    public void testGetByPermIdNonexistent()
    {
        final ProjectPermId permId = new ProjectPermId("NONEXISTENT_PROJECT");

        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);

                    one(projectDao).listByPermID(Collections.singletonList(permId.getPermId()));
                    will(returnValue(null));
                }
            });

        Map<IProjectId, ProjectPE> result = execute(permId);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testGetByIdentifier()
    {
        final SpacePE space = new SpacePE();
        space.setCode("TEST_SPACE");

        final ProjectPE project = new ProjectPE();
        project.setId(123L);
        project.setCode("TEST_PROJECT");
        project.setSpace(space);

        final ProjectIdentifier identifier = new ProjectIdentifier("/" + space.getCode() + "/" + project.getCode());

        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);

                    one(projectDao).tryFindProjects(
                            Collections.singletonList(new ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier(space.getCode(),
                                    project.getCode())));
                    will(returnValue(Collections.singletonList(project)));
                }
            });

        Map<IProjectId, ProjectPE> result = execute(identifier);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Long.valueOf(123L), result.get(identifier).getId());
    }

    @Test
    public void testGetByIdentifierNonexistent()
    {
        final String spaceCode = "TEST_SPACE";
        final String projectCode = "NONEXISTENT_PROJECT";
        final ProjectIdentifier identifier = new ProjectIdentifier("/" + spaceCode + "/" + projectCode);

        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);

                    one(projectDao).tryFindProjects(
                            Collections.singletonList(new ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier(spaceCode,
                                    projectCode)));
                    will(returnValue(Collections.emptyList()));
                }
            });

        Map<IProjectId, ProjectPE> result = execute(identifier);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test(expectedExceptions = { UnsupportedObjectIdException.class })
    public void testGetByUnknownId()
    {
        final IProjectId unknownId = new IProjectId()
            {

                private static final long serialVersionUID = 1L;

            };

        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);
                }
            });

        execute(unknownId);
    }

    private Map<IProjectId, ProjectPE> execute(IProjectId... projectIds)
    {
        IMapProjectByIdExecutor executor = new MapProjectByIdExecutor(projectDao, authorizationExecutor);
        return executor.map(operationContext, Arrays.asList(projectIds));
    }
}