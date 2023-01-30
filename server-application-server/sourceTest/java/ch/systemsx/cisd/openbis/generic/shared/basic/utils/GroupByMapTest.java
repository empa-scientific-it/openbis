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

package ch.systemsx.cisd.openbis.generic.shared.basic.utils;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Tests of {@link GroupByMap}.
 * 
 * @author Tomasz Pylak
 */
public class GroupByMapTest extends AssertJUnit
{
    private IGroupKeyExtractor<Integer, Integer> createKeyExtractor()
    {
        return new IGroupKeyExtractor<Integer, Integer>()
            {
                @Override
                public Integer getKey(Integer e)
                {
                    return e;
                }
            };
    }

    @Test
    public void test()
    {
        List<Integer> list = Arrays.asList(new Integer[]
        { 1, 100, 1, 5, 100, 100 });
        GroupByMap<Integer, Integer> map = GroupByMap.create(list, createKeyExtractor());
        assertNull(map.tryGet(9));

        List<Integer> list100 = map.tryGet(100);
        assertNotNull(list100);
        assertEquals(3, list100.size());

        List<Integer> list5 = map.tryGet(5);
        assertNotNull(list5);
        assertEquals(1, list5.size());
    }
}
