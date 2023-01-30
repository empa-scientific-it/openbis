/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter.DataSetDataLoader;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter.IEntityDataLoader;

/**
 * Test cases for {@link DataSetDataLoader}.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetGroupLoaderTest extends AssertJUnit
{
    private static final String UNKNOWN = "unknown";

    private static final String KNOWN_WITHOUT_GROUP = "known-without-group";

    private static final String KNOWN_WITH_GROUP = "known-with-group";

    private Mockery context;

    private IDataDAO dao;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dao = context.mock(IDataDAO.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    IEntityDataLoader createLoader()
    {
        return new DataSetDataLoader(dao);
    }

    @Test
    public void testLoadGroups() throws Exception
    {
        final HashSet<String> keys = new HashSet<String>();
        keys.add(KNOWN_WITH_GROUP);
        keys.add(KNOWN_WITHOUT_GROUP);
        keys.add(UNKNOWN);
        final ArrayList<ExternalDataPE> datasets = new ArrayList<ExternalDataPE>();
        SpacePE knownGroup = new SpacePE();
        datasets.add(createDataset(KNOWN_WITH_GROUP, knownGroup));
        datasets.add(createDataset(KNOWN_WITHOUT_GROUP, null));
        context.checking(new Expectations()
            {
                {
                    one(dao).listByCode(keys);
                    will(returnValue(datasets));
                }
            });
        Map<String, SpacePE> map = createLoader().loadGroups(keys);
        assertNull(map.get(UNKNOWN));
        assertNull(map.get(KNOWN_WITHOUT_GROUP));
        assertEquals(knownGroup, map.get(KNOWN_WITH_GROUP));
        context.assertIsSatisfied();
    }

    private ExternalDataPE createDataset(String code, SpacePE group)
    {
        ExternalDataPE dataset = new ExternalDataPE();
        ExperimentPE experiment = new ExperimentPE();
        ProjectPE project = new ProjectPE();
        project.setSpace(group);
        experiment.setProject(project);
        dataset.setExperiment(experiment);
        dataset.setCode(code);
        return dataset;
    }
}
