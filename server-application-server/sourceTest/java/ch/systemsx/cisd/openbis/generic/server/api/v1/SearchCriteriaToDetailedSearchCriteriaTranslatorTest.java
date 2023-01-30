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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.api.v1.SearchCriteriaToDetailedSearchCriteriaTranslator.IMatchClauseAttributeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SearchCriteriaToDetailedSearchCriteriaTranslatorTest extends AssertJUnit
{
    @Test
    public void testTranslatorsInitialization()
    {
        for (SearchableEntityKind entityKind : SearchableEntityKind.values())
        {
            IMatchClauseAttributeTranslator translator =
                    SearchCriteriaToDetailedSearchCriteriaTranslator.translators.get(entityKind);
            assertNotNull("No translator defined for " + entityKind, translator);
        }
    }

    @Test
    public void testBasicMatchAllCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        SearchCriteriaToDetailedSearchCriteriaTranslator translator =
                new SearchCriteriaToDetailedSearchCriteriaTranslator(null, criteria,
                        SearchableEntityKind.SAMPLE);

        DetailedSearchCriteria detailedSearchCriteria =
                translator.convertToDetailedSearchCriteria();
        assertNotNull(detailedSearchCriteria);

        // It is easier to test equality by string comparison
        assertEquals("ATTRIBUTE CODE: a code AND " + "PROPERTY MY_PROPERTY: a property value "
                + "(with wildcards)", detailedSearchCriteria.toString());
    }

    @Test
    public void testBasicMatchAnyCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        criteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
        SearchCriteriaToDetailedSearchCriteriaTranslator translator =
                new SearchCriteriaToDetailedSearchCriteriaTranslator(null, criteria,
                        SearchableEntityKind.SAMPLE);

        DetailedSearchCriteria detailedSearchCriteria =
                translator.convertToDetailedSearchCriteria();
        assertNotNull(detailedSearchCriteria);

        assertEquals("ATTRIBUTE CODE: a code OR " + "PROPERTY MY_PROPERTY: a property value "
                + "(with wildcards)", detailedSearchCriteria.toString());
    }

    @Test
    public void testFullSampleMatchAllCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "a type"));
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE,
                "a space"));
        SearchCriteriaToDetailedSearchCriteriaTranslator translator =
                new SearchCriteriaToDetailedSearchCriteriaTranslator(null, criteria,
                        SearchableEntityKind.SAMPLE);

        DetailedSearchCriteria detailedSearchCriteria =
                translator.convertToDetailedSearchCriteria();
        assertNotNull(detailedSearchCriteria);

        assertEquals("ATTRIBUTE CODE: a code AND " + "PROPERTY MY_PROPERTY: a property value AND "
                + "ATTRIBUTE SAMPLE_TYPE: a type AND " + "ATTRIBUTE SPACE: a space "
                + "(with wildcards)", detailedSearchCriteria.toString());
    }

    @Test
    public void testFullExperimentMatchAllCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "a type"));
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE,
                "a space"));
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT,
                "a project"));
        SearchCriteriaToDetailedSearchCriteriaTranslator translator =
                new SearchCriteriaToDetailedSearchCriteriaTranslator(null, criteria,
                        SearchableEntityKind.EXPERIMENT);

        DetailedSearchCriteria detailedSearchCriteria =
                translator.convertToDetailedSearchCriteria();
        assertNotNull(detailedSearchCriteria);

        assertEquals("ATTRIBUTE CODE: a code AND " + "PROPERTY MY_PROPERTY: a property value AND "
                + "ATTRIBUTE EXPERIMENT_TYPE: a type AND "
                + "ATTRIBUTE PROJECT_SPACE: a space AND " + "ATTRIBUTE PROJECT: a project "
                + "(with wildcards)", detailedSearchCriteria.toString());
    }

    @Test
    public void testSampleCriteriaTranslatorFailsWithUnsupportedAttribute()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT,
                "a project"));
        SearchCriteriaToDetailedSearchCriteriaTranslator translator =
                new SearchCriteriaToDetailedSearchCriteriaTranslator(null, criteria,
                        SearchableEntityKind.SAMPLE);
        try
        {
            translator.convertToDetailedSearchCriteria();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex)
        {
            assertEquals("PROJECT is not a valid search attribute for SAMPLE", ex.getMessage());
        }
    }

    private static String EXPECTED_BASIC_QUERY_SUFFIX = "ATTRIBUTE CODE: a code AND "
            + "PROPERTY MY_PROPERTY: a property value";

    private void testConvertToDetailedSearchSubCriteria(AssociatedEntityKind expectedEntityKind,
            SearchSubCriteria subCriteria)
    {
        DetailedSearchSubCriteria detailedSearchSubCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator
                        .convertToDetailedSearchSubCriteria(null, subCriteria);
        assertNotNull(detailedSearchSubCriteria);
        assertEquals(expectedEntityKind + ": " + EXPECTED_BASIC_QUERY_SUFFIX,
                detailedSearchSubCriteria.toString());
    }

    @Test
    public void testBasicSampleSubCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        SearchSubCriteria subCriteria = SearchSubCriteria.createSampleCriteria(criteria);
        testConvertToDetailedSearchSubCriteria(AssociatedEntityKind.SAMPLE, subCriteria);
    }

    @Test
    public void testBasicSampleParentSubCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        SearchSubCriteria subCriteria = SearchSubCriteria.createSampleParentCriteria(criteria);
        testConvertToDetailedSearchSubCriteria(AssociatedEntityKind.SAMPLE_PARENT, subCriteria);
    }

    @Test
    public void testBasicSampleChildSubCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        SearchSubCriteria subCriteria = SearchSubCriteria.createSampleChildCriteria(criteria);
        testConvertToDetailedSearchSubCriteria(AssociatedEntityKind.SAMPLE_CHILD, subCriteria);
    }

    @Test
    public void testBasicSampleContainerSubCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        SearchSubCriteria subCriteria = SearchSubCriteria.createSampleContainerCriteria(criteria);
        testConvertToDetailedSearchSubCriteria(AssociatedEntityKind.SAMPLE_CONTAINER, subCriteria);
    }

    @Test
    public void testBasicDataSetParentSubCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        SearchSubCriteria subCriteria = SearchSubCriteria.createDataSetParentCriteria(criteria);
        testConvertToDetailedSearchSubCriteria(AssociatedEntityKind.DATA_SET_PARENT, subCriteria);
    }

    @Test
    public void testBasicDataSetChildSubCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        SearchSubCriteria subCriteria = SearchSubCriteria.createDataSetChildCriteria(criteria);
        testConvertToDetailedSearchSubCriteria(AssociatedEntityKind.DATA_SET_CHILD, subCriteria);
    }

    @Test
    public void testBasicDataSetContainerSubCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        SearchSubCriteria subCriteria = SearchSubCriteria.createDataSetContainerCriteria(criteria);
        testConvertToDetailedSearchSubCriteria(AssociatedEntityKind.DATA_SET_CONTAINER, subCriteria);
    }

    @Test
    public void testBasicExperimentSubCriteriaTranslator()
    {
        SearchCriteria criteria = createBasicSearchCriteria();
        SearchSubCriteria subCriteria = SearchSubCriteria.createExperimentCriteria(criteria);
        testConvertToDetailedSearchSubCriteria(AssociatedEntityKind.EXPERIMENT, subCriteria);
    }

    private SearchCriteria createBasicSearchCriteria()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "a code"));
        sc.addMatchClause(MatchClause.createPropertyMatch("MY_PROPERTY", "a property value"));
        return sc;
    }

}
