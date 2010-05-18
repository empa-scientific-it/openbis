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

package ch.systemsx.cisd.openbis.plugin.query.shared.authorization.result_filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Test cases for {@link SampleGroupLoader}.
 * 
 * @author Izabela Adamczyk
 */
public class SampleGroupLoaderTest extends AssertJUnit
{
    private static final String UNKNOWN = "unknown";

    private static final String KNOWN_WITHOUT_GROUP = "known-without-group";

    private static final String KNOWN_WITH_GROUP = "known-with-group";

    private Mockery context;

    private ISampleDAO dao;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dao = context.mock(ISampleDAO.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    IGroupLoader createLoader()
    {
        return new SampleGroupLoader(dao);
    }

    @Test
    public void testLoadGroups() throws Exception
    {
        final HashSet<String> keys = new HashSet<String>();
        keys.add(KNOWN_WITH_GROUP);
        keys.add(KNOWN_WITHOUT_GROUP);
        keys.add(UNKNOWN);
        final ArrayList<SamplePE> samples = new ArrayList<SamplePE>();
        GroupPE knownGroup = new GroupPE();
        samples.add(createSample(KNOWN_WITH_GROUP, knownGroup));
        samples.add(createSample(KNOWN_WITHOUT_GROUP, null));
        context.checking(new Expectations()
            {
                {
                    one(dao).listByPermID(keys);
                    will(returnValue(samples));
                }
            });
        Map<String, GroupPE> map = createLoader().loadGroups(keys);
        assertNull(map.get(UNKNOWN));
        assertNull(map.get(KNOWN_WITHOUT_GROUP));
        assertEquals(knownGroup, map.get(KNOWN_WITH_GROUP));
        context.assertIsSatisfied();
    }

    private SamplePE createSample(String permId, GroupPE group)
    {
        SamplePE sample = new SamplePE();
        sample.setGroup(group);
        sample.setPermId(permId);
        return sample;
    }
}
