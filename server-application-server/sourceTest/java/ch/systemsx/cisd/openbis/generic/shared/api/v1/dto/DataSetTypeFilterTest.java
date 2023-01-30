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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType.DataSetTypeInitializer;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetTypeFilterTest extends AssertJUnit
{
    // The full list of types initialized in set up
    private ArrayList<DataSetType> dataSetTypes;

    // Convenience ivars that are modified in each test method
    DataSetTypeFilter filter;

    List<DataSetType> filteredTypes;

    @BeforeTest
    public void setUp()
    {
        initializeDataSetTypes();
    }

    @Test
    public void testFilterStringSerialization()
    {
        List<String> filterStrings;
        String filterListString;

        filterListString = DataSetTypeFilter.convertPatternListToString(null);
        assertEquals("", filterListString);

        filterStrings = Arrays.asList();
        filterListString = DataSetTypeFilter.convertPatternListToString(filterStrings);
        assertEquals("", filterListString);

        filterStrings = Arrays.asList("");
        filterListString = DataSetTypeFilter.convertPatternListToString(filterStrings);
        assertEquals("", filterListString);

        filterStrings = Arrays.asList("foo");
        filterListString = DataSetTypeFilter.convertPatternListToString(filterStrings);
        assertEquals("foo", filterListString);

        filterStrings = Arrays.asList("foo*", "*bar");
        filterListString = DataSetTypeFilter.convertPatternListToString(filterStrings);
        assertEquals("foo*,*bar", filterListString);
    }

    @Test
    public void testWhitelistFilter()
    {
        applyFilter(".*", "");
        assertEquals(dataSetTypes.size(), filteredTypes.size());

        applyFilter("data-set-type-.", "");
        assertEquals(2, filteredTypes.size());
        assertTrue(isContainedInFilteredTypes("data-set-type-1"));
        assertTrue(isContainedInFilteredTypes("data-set-type-2"));

        applyFilter("data-set-type-[0-9]?", "");
        assertEquals(2, filteredTypes.size());
        assertTrue(isContainedInFilteredTypes("data-set-type-1"));
        assertTrue(isContainedInFilteredTypes("data-set-type-2"));

        applyFilter("2-data-set-type", "");
        assertEquals(1, filteredTypes.size());
        assertTrue(isContainedInFilteredTypes("2-data-set-type"));

        applyFilter("1-data-set-type,data-set-type-2", "");
        assertEquals(2, filteredTypes.size());
        assertTrue(isContainedInFilteredTypes("1-data-set-type"));
        assertTrue(isContainedInFilteredTypes("data-set-type-2"));
    }

    @Test
    public void testBlacklistFilter()
    {
        applyFilter("", ".*");
        assertEquals(0, filteredTypes.size());

        applyFilter("", "data-set-type-.");
        assertEquals(dataSetTypes.size() - 2, filteredTypes.size());
        assertTrue(notContainedInFilteredTypes("data-set-type-1"));
        assertTrue(notContainedInFilteredTypes("data-set-type-2"));

        applyFilter("", "2-data-set-type");
        assertEquals(dataSetTypes.size() - 1, filteredTypes.size());
        assertTrue(notContainedInFilteredTypes("2-data-set-type"));

        applyFilter("", "1-data-set-type,data-set-type-2");
        assertEquals(dataSetTypes.size() - 2, filteredTypes.size());
        assertTrue(notContainedInFilteredTypes("1-data-set-type"));
        assertTrue(notContainedInFilteredTypes("data-set-type-2"));
    }

    @Test
    public void testNoFilter()
    {
        applyFilter("", "");
        assertEquals(dataSetTypes.size(), filteredTypes.size());
    }

    private void applyFilter(String whitelist, String blacklist)
    {
        filter = new DataSetTypeFilter(whitelist, blacklist);
        filteredTypes = filter.filterDataSetTypes(dataSetTypes);
    }

    private boolean isContainedInFilteredTypes(String dataSetTypeCode)
    {
        for (DataSetType type : filteredTypes)
        {
            if (dataSetTypeCode.equals(type.getCode()))
            {
                return true;
            }
        }

        return false;
    }

    private boolean notContainedInFilteredTypes(String dataSetTypeCode)
    {
        for (DataSetType type : filteredTypes)
        {
            if (dataSetTypeCode.equals(type.getCode()))
            {
                return false;
            }
        }

        return true;
    }

    private void initializeDataSetTypes()
    {
        dataSetTypes = new ArrayList<DataSetType>();
        DataSetTypeInitializer initializer;

        initializer = new DataSetTypeInitializer();
        initializer.setCode("data-set-type-1");
        dataSetTypes.add(new DataSetType(initializer));

        initializer = new DataSetTypeInitializer();
        initializer.setCode("data-set-type-2");
        dataSetTypes.add(new DataSetType(initializer));

        initializer = new DataSetTypeInitializer();
        initializer.setCode("1-data-set-type");
        dataSetTypes.add(new DataSetType(initializer));

        initializer = new DataSetTypeInitializer();
        initializer.setCode("2-data-set-type");
        dataSetTypes.add(new DataSetType(initializer));

        initializer = new DataSetTypeInitializer();
        initializer.setCode("data-1-set-type");
        dataSetTypes.add(new DataSetType(initializer));

        initializer = new DataSetTypeInitializer();
        initializer.setCode("data-2-set-type");
        dataSetTypes.add(new DataSetType(initializer));
    }
}
