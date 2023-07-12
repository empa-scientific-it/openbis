/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.business.search.sort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;

public class SearchResultSorterTestHelper
{
    public static class EntitySearchResult implements IEntitySearchResult
    {
        private final String code;

        private final String typeCode;

        private final Map<String, String> properties;

        public EntitySearchResult(
                String code,
                String typeCode,
                Map<String, String> properties)
        {
            this.code = code;
            this.typeCode = typeCode;
            this.properties = properties;
        }

        public String getCode()
        {
            return code;
        }

        public String getTypeCode()
        {
            return typeCode;
        }

        public Map<String, String> getProperties()
        {
            return properties;
        }

    }

    public static DetailedSearchCriterion getAnyFieldCriterion(String value)
    {
        return new DetailedSearchCriterion(DetailedSearchField.createAnyField(Arrays.asList("ANY")), value);
    }

    public static DetailedSearchCriterion getPropertyFieldCriterion(String propertyCode, String value)
    {
        return new DetailedSearchCriterion(DetailedSearchField.createPropertyField(propertyCode), value);
    }

    public static DetailedSearchCriterion getAnyPropertyFieldCriterion(String value)
    {
        return new DetailedSearchCriterion(DetailedSearchField.createAnyPropertyField(Arrays.asList("ANY")), value);
    }

    public static DetailedSearchCriterion getCodeFieldCriterion(String value)
    {
        // TODO refactor it not to use sample specific class
        return new DetailedSearchCriterion(DetailedSearchField.createAttributeField(SampleAttributeSearchFieldKind.CODE), value);
    }

    public static DetailedSearchCriterion getTypeCodeFieldCriterion(String value)
    {
        // TODO refactor it not to use sample specific class
        return new DetailedSearchCriterion(DetailedSearchField.createAttributeField(SampleAttributeSearchFieldKind.SAMPLE_TYPE), value);
    }

    public static EntitySearchResult createEntity(String code, String typeCode, String... propertyValues)
    {
        Map<String, String> properties = new HashMap<String, String>();
        int propertyIndex = 1;
        for (String propertyValue : propertyValues)
        {
            properties.put("PROP_" + propertyIndex, propertyValue);
            propertyIndex++;
        }

        return new EntitySearchResult(code, typeCode, properties);
    }

    public static void sort(List<EntitySearchResult> entities, DetailedSearchCriterion... searchCriterions)
    {
        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        List<DetailedSearchCriterion> criterions = new ArrayList<DetailedSearchCriterion>();
        criteria.setCriteria(criterions);

        for (DetailedSearchCriterion searchCriterion : searchCriterions)
        {
            criterions.add(searchCriterion);
        }

        SearchResultSorterByScore sorter = new SearchResultSorterByScore();
        sorter.sort(entities, criteria);
    }

    public static void assertEntities(List<EntitySearchResult> actualEntities, String... expectedCodes)
    {
        List<String> actualCodes = new ArrayList<String>();

        for (EntitySearchResult actualEntity : actualEntities)
        {
            actualCodes.add(actualEntity.getCode());
        }

        Assert.assertEquals(Arrays.asList(expectedCodes), actualCodes);
    }
}
