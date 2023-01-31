/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class DataSourceUtilsTest
{

    @Test
    public void testGetRquestedAndAllowedSubSetWithUnspecifiedWhiteAndBlackList()
    {
        // Given
        List<String> fullSet = Arrays.asList("abc", "age", "Affe");
        List<String> whiteLists = null;
        List<String> blackLists = null;

        // When
        Set<String> subSet = DataSourceUtils.getRequestedAndAllowedSubSet(fullSet, whiteLists, blackLists);

        // Then
        List<String> sortedSubSet = new ArrayList<String>(subSet);
        Collections.sort(sortedSubSet);
        assertEquals(sortedSubSet.toString(), "[Affe, abc, age]");
    }

    @Test
    public void testGetRquestedAndAllowedSubSetWithBlackListAndUnspecifiedWhiteList()
    {
        // Given
        List<String> fullSet = Arrays.asList("abc", "age", "abcdef");
        List<String> whiteLists = null;
        List<String> blackLists = Arrays.asList("a, age", ".*f");

        // When
        Set<String> subSet = DataSourceUtils.getRequestedAndAllowedSubSet(fullSet, whiteLists, blackLists);

        // Then
        List<String> sortedSubSet = new ArrayList<String>(subSet);
        Collections.sort(sortedSubSet);
        assertEquals(sortedSubSet.toString(), "[abc]");
    }

    @Test
    public void testGetRquestedAndAllowedSubSetWithWhiteListAndUnspecifiedBlackList()
    {
        // Given
        List<String> fullSet = Arrays.asList("abc", "age", "abcdef");
        List<String> whiteLists = Arrays.asList("age", ".*f, a");
        List<String> blackLists = null;

        // When
        Set<String> subSet = DataSourceUtils.getRequestedAndAllowedSubSet(fullSet, whiteLists, blackLists);

        // Then
        List<String> sortedSubSet = new ArrayList<String>(subSet);
        Collections.sort(sortedSubSet);
        assertEquals(sortedSubSet.toString(), "[abcdef, age]");
    }

    @Test
    public void testGetRquestedAndAllowedSubSetWithWhiteListAndBlackList()
    {
        // Given
        List<String> fullSet = Arrays.asList("abc", "a", "age", "abcdef", "defg");
        List<String> whiteLists = Arrays.asList("hijk", "a.*");
        List<String> blackLists = Arrays.asList("age, .*f, a");

        // When
        Set<String> subSet = DataSourceUtils.getRequestedAndAllowedSubSet(fullSet, whiteLists, blackLists);

        // Then
        List<String> sortedSubSet = new ArrayList<String>(subSet);
        Collections.sort(sortedSubSet);
        assertEquals(sortedSubSet.toString(), "[abc]");
    }

}
