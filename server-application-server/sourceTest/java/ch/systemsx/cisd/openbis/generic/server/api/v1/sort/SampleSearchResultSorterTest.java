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
package ch.systemsx.cisd.openbis.generic.server.api.v1.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;

/**
 * Test Class for SampleSearchResultSorter.
 * 
 * @author pkupczyk
 * @author juanf
 */
public class SampleSearchResultSorterTest extends AssertJUnit
{

    @Test
    public void testAll()
    {
        //
        // Samples Setup
        //
        List<Sample> samples = new ArrayList<Sample>();
        samples.add(createSample("CODE_2", "TYPE_2", "ABC_1", "ABC_2"));
        samples.add(createSample("CODE_1", "TYPE_1", "ABC_1"));
        samples.add(createSample("CODE_3", "TYPE_3", "ABC_1", "ABC_2", "ABC_3"));
        samples.add(createSample("CODE_4", "TYPE_4", "TYU"));
        samples.add(createSample("CODE_5", "TYPE_5", "XYZ"));
        samples.add(createSample("CODE_6", "TYPE_6", "666"));
        samples.add(createSample("CODE_7", "TYPE_7"));

        DetailedSearchCriteria criteria = new DetailedSearchCriteria();

        List<DetailedSearchCriterion> criterions = new ArrayList<DetailedSearchCriterion>();
        // Test hit only type
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "TYPE_6"));
        // Test hit only properties
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "ABC"));
        // Test hit only code
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "CODE_4"));
        // Test hit property and code
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "CODE_5"));
        criterions.add(new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), "XYZ"));
        criteria.setCriteria(criterions);

        //
        // Run algorithm
        //
        SampleSearchResultSorter sorter = new SampleSearchResultSorter();
        samples = sorter.sort(samples, criteria);

        //
        // Verify Resuls
        //
        assertEquals("CODE_5", samples.get(0).getCode());
        assertEquals("CODE_4", samples.get(1).getCode());
        assertEquals("CODE_6", samples.get(2).getCode());
        assertEquals("CODE_3", samples.get(3).getCode());
        assertEquals("CODE_2", samples.get(4).getCode());
        assertEquals("CODE_1", samples.get(5).getCode());
        assertEquals("CODE_7", samples.get(6).getCode());
    }

    private Sample createSample(String code, String typeCode, String... propertyValues)
    {
        SampleInitializer sampleInitializer = new SampleInitializer();
        sampleInitializer.setIdentifier("testIdentifier");
        sampleInitializer.setId(-1L);
        sampleInitializer.setPermId("testPermId");
        sampleInitializer.setSampleTypeId(-1L);
        sampleInitializer.setCode(code);
        sampleInitializer.setSampleTypeCode(typeCode);
        sampleInitializer.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));

        if (propertyValues != null)
        {
            sampleInitializer.setRetrievedFetchOptions(EnumSet.of(SampleFetchOption.PROPERTIES));

            int propertyIndex = 1;
            for (String propertyValue : propertyValues)
            {
                sampleInitializer.putProperty("PROP_" + propertyIndex, propertyValue);
                propertyIndex++;
            }
        }

        return new Sample(sampleInitializer);
    }
}
