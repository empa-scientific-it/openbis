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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import java.util.Arrays;
import java.util.Map;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.AbstractExecutorTest;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.MapExperimentByIdExecutor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class MapExperimentByIdExecutorTest extends AbstractExecutorTest
{

    private IProjectDAO projectDao;

    private IExperimentDAO experimentDao;

    private IExperimentAuthorizationExecutor authorizationExecutor;

    @Override
    protected void init()
    {
        projectDao = context.mock(IProjectDAO.class);
        experimentDao = context.mock(IExperimentDAO.class);
        authorizationExecutor = context.mock(IExperimentAuthorizationExecutor.class);
    }

    @Test
    public void testMapWithPermIds()
    {
        final ExperimentPermId permId1 = new ExperimentPermId("PERM_1");
        final ExperimentPermId permId2 = new ExperimentPermId("PERM_2");
        final ExperimentPermId permId3 = new ExperimentPermId("PERM_3");

        final ExperimentPE experiment1 = new ExperimentPE();
        experiment1.setCode("EXP_1");
        experiment1.setPermId(permId1.getPermId());
        final ExperimentPE experiment2 = new ExperimentPE();
        experiment2.setCode("EXP_2");
        experiment2.setPermId(permId2.getPermId());
        final ExperimentPE experiment3 = new ExperimentPE();
        experiment3.setCode("EXP_3");
        experiment3.setPermId(permId3.getPermId());

        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);
                    one(experimentDao).listByPermID(Arrays.asList(permId1.getPermId(), permId2.getPermId(), permId3.getPermId()));
                    will(returnValue(Arrays.asList(experiment2, experiment1, experiment3)));
                }
            });

        Map<IExperimentId, ExperimentPE> map = execute(permId1, permId2, permId3);

        Assert.assertTrue(experiment1 == map.get(permId1));
        Assert.assertTrue(experiment2 == map.get(permId2));
        Assert.assertTrue(experiment3 == map.get(permId3));
    }

    @Test
    public void testMapWithIdentifiers()
    {
        final SpacePE space1 = new SpacePE();
        space1.setCode("SPACE_1");
        final SpacePE space2 = new SpacePE();
        space2.setCode("SPACE_2");

        final ProjectPE project11 = new ProjectPE();
        project11.setSpace(space1);
        project11.setCode("PROJECT_1");
        final ProjectPE project12 = new ProjectPE();
        project12.setSpace(space1);
        project12.setCode("PROJECT_2");
        final ProjectPE project21 = new ProjectPE();
        project21.setSpace(space2);
        project21.setCode("PROJECT_1");

        final ExperimentIdentifier identifier1 = new ExperimentIdentifier("/SPACE_1/PROJECT_1/EXPERIMENT_1");
        final ExperimentIdentifier identifier2 = new ExperimentIdentifier("/SPACE_1/PROJECT_1/EXPERIMENT_2");
        final ExperimentIdentifier identifier3 = new ExperimentIdentifier("/SPACE_1/PROJECT_2/EXPERIMENT_3");
        final ExperimentIdentifier identifier4 = new ExperimentIdentifier("/SPACE_2/PROJECT_1/EXPERIMENT_4");

        final ExperimentPE experiment1 = new ExperimentPE();
        experiment1.setProject(project11);
        experiment1.setCode("EXPERIMENT_1");
        final ExperimentPE experiment2 = new ExperimentPE();
        experiment2.setProject(project11);
        experiment2.setCode("EXPERIMENT_2");
        final ExperimentPE experiment3 = new ExperimentPE();
        experiment3.setProject(project12);
        experiment3.setCode("EXPERIMENT_3");
        final ExperimentPE experiment4 = new ExperimentPE();
        experiment4.setProject(project21);
        experiment4.setCode("EXPERIMENT_4");

        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);

                    one(projectDao).tryFindProject("SPACE_1", "PROJECT_1");
                    will(returnValue(project11));

                    one(projectDao).tryFindProject("SPACE_1", "PROJECT_2");
                    will(returnValue(project12));

                    one(projectDao).tryFindProject("SPACE_2", "PROJECT_1");
                    will(returnValue(project21));

                    one(experimentDao).listByProjectAndCodes(project11, Arrays.asList("EXPERIMENT_1", "EXPERIMENT_2"));
                    will(returnValue(Arrays.asList(experiment1, experiment2)));

                    one(experimentDao).listByProjectAndCodes(project12, Arrays.asList("EXPERIMENT_3"));
                    will(returnValue(Arrays.asList(experiment3)));

                    one(experimentDao).listByProjectAndCodes(project21, Arrays.asList("EXPERIMENT_4"));
                    will(returnValue(Arrays.asList(experiment4)));
                }
            });

        Map<IExperimentId, ExperimentPE> map = execute(identifier1, identifier2, identifier3, identifier4);

        Assert.assertTrue(experiment1 == map.get(identifier1));
        Assert.assertTrue(experiment2 == map.get(identifier2));
        Assert.assertTrue(experiment3 == map.get(identifier3));
        Assert.assertTrue(experiment4 == map.get(identifier4));
    }

    @Test
    public void testMapWithMixedIds()
    {
        final SpacePE space1 = new SpacePE();
        space1.setCode("SPACE_1");

        final ProjectPE project11 = new ProjectPE();
        project11.setSpace(space1);
        project11.setCode("PROJECT_1");

        final ExperimentPE experiment1 = new ExperimentPE();
        experiment1.setProject(project11);
        experiment1.setCode("EXPERIMENT_1");

        final ExperimentPE experiment2 = new ExperimentPE();
        experiment2.setCode("EXPERIMENT_2");
        experiment2.setPermId("PERM_1");

        final ExperimentPE experiment3 = new ExperimentPE();
        experiment3.setCode("EXPERIMENT_3");
        experiment3.setPermId("PERM_2");

        final ExperimentIdentifier identifier1 = new ExperimentIdentifier("/SPACE_1/PROJECT_1/EXPERIMENT_1");
        final ExperimentPermId permId1 = new ExperimentPermId("PERM_1");
        final ExperimentPermId permId2 = new ExperimentPermId("PERM_2");

        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);

                    one(experimentDao).listByPermID(Arrays.asList(permId1.getPermId(), permId2.getPermId()));
                    will(returnValue(Arrays.asList(experiment3, experiment2)));

                    one(projectDao).tryFindProject("SPACE_1", "PROJECT_1");
                    will(returnValue(project11));

                    one(experimentDao).listByProjectAndCodes(project11, Arrays.asList("EXPERIMENT_1"));
                    will(returnValue(Arrays.asList(experiment1)));
                }
            });

        Map<IExperimentId, ExperimentPE> map = execute(permId1, identifier1, permId2);

        Assert.assertTrue(experiment1 == map.get(identifier1));
        Assert.assertTrue(experiment2 == map.get(permId1));
        Assert.assertTrue(experiment3 == map.get(permId2));
    }

    private Map<IExperimentId, ExperimentPE> execute(IExperimentId... experimentIds)
    {
        MapExperimentByIdExecutor executor = new MapExperimentByIdExecutor(projectDao, experimentDao, authorizationExecutor);
        return executor.map(operationContext, Arrays.asList(experimentIds));
    }
}
