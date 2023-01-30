/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * @author Tomasz Pylak
 */
public class EntityHelperTest extends AssertJUnit
{
    @Test
    public void removePropertyTest()
    {
        List<NewProperty> properties = new ArrayList<NewProperty>();
        properties.add(new NewProperty("A", "1"));
        properties.add(new NewProperty("B", "2"));
        EntityHelper.removeProperty(properties, "B");
        assertEquals(1, properties.size());
        assertEquals("A", properties.get(0).getPropertyCode());
    }

    @Test
    public void testEqualEntities()
    {
        assertEquals(true, EntityHelper.equalEntities(null, null));
        assertEquals(false, EntityHelper.equalEntities(null, new TechId(1)));
        assertEquals(true, EntityHelper.equalEntities(new TechId(1), new TechId(1)));
        assertEquals(false, EntityHelper.equalEntities(new TechId(2), new TechId(1)));
        assertEquals(false, EntityHelper.equalEntities(new TechId(2), null));
        assertEquals(false, EntityHelper.equalEntities(new SampleBuilder().id(1).getSample(), new TechId(1)));
        assertEquals(true, EntityHelper.equalEntities(new Sample(), new Sample()));
        assertEquals(false, EntityHelper.equalEntities(null, new Sample()));
        assertEquals(false, EntityHelper.equalEntities(new Sample(), null));
    }
}
