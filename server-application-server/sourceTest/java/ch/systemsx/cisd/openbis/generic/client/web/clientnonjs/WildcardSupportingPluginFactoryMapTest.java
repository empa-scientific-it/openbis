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

package ch.systemsx.cisd.openbis.generic.client.web.clientnonjs;

import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.WildcardSupportingPluginFactoryMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKindAndTypeCode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class WildcardSupportingPluginFactoryMapTest extends AssertJUnit
{

    private WildcardSupportingPluginFactoryMap map;

    private Mockery context;

    private IClientPluginFactory factory1;

    private IClientPluginFactory factory2;

    @BeforeMethod
    public void setUp()
    {
        map = new WildcardSupportingPluginFactoryMap();
        context = new Mockery();
        factory1 = context.mock(IClientPluginFactory.class, "Factory 1");
        factory2 = context.mock(IClientPluginFactory.class, "Factory 2");
    }

    @Test
    public void testNonWildcardMapping()
    {
        // Setup some mappings
        map.addMapping(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCode1"), factory1);
        map.addMapping(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCode2"), factory2);
        map.addMapping(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode1"), factory1);
        map.addMapping(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode2"), factory2);
        map.addMapping(new EntityKindAndTypeCode(EntityKind.DATA_SET, "sampleCode1"), factory2);

        // Make sure the entitykind/code returns the mapping we set up
        assertEquals(factory1,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCode1")));
        assertEquals(factory2,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCode2")));
        assertEquals(factory1,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode1")));
        assertEquals(factory2,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode2")));
        assertEquals(factory2,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.DATA_SET, "sampleCode1")));

        // Make sure matches are exact
        assertNull(map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCode")));
        assertNull(map
                .tryPluginFactory(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCode11")));
        assertNull(map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.DATA_SET, "*Code*")));

    }

    @Test
    public void testWildcardMapping()
    {
        // Setup some mappings
        map.addMapping(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCode.*"), factory1);
        map.addMapping(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCode2"), factory2);
        map.addMapping(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode1"), factory1);
        map.addMapping(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, ".+Code.*"), factory2);
        map.addMapping(new EntityKindAndTypeCode(EntityKind.DATA_SET, "sampleCode.+"), factory2);

        // Make sure the entitykind/code returns the mapping we set up
        assertEquals(factory1,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCode1")));
        assertEquals(factory1,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCode2")));
        assertEquals(factory1,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.SAMPLE, "sampleCodes")));
        assertEquals(factory1,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode1")));
        assertEquals(factory2,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode2")));
        assertEquals(factory2,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode3")));
        assertEquals(factory2,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.DATA_SET, "sampleCode1")));
        assertEquals(factory2,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.DATA_SET, "sampleCode*")));

        // Make sure matches are exact
        assertNull(map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.SAMPLE, "a")));
        assertNull(map
                .tryPluginFactory(new EntityKindAndTypeCode(EntityKind.DATA_SET, "sampleCode")));
    }

    @Test
    void testMappingOrdering()
    {
        map.addMapping(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode1"), factory1);
        map.addMapping(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode11"), factory1);
        map.addMapping(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode11*"), factory2);

        assertEquals(factory1,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode1")));
        assertEquals(factory1,
                map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.EXPERIMENT, "expCode11")));
        assertEquals(factory2, map.tryPluginFactory(new EntityKindAndTypeCode(
                EntityKind.EXPERIMENT, "expCode111")));

        assertNull(map.tryPluginFactory(new EntityKindAndTypeCode(EntityKind.EXPERIMENT,
                "expCode11a")));
    }
}
