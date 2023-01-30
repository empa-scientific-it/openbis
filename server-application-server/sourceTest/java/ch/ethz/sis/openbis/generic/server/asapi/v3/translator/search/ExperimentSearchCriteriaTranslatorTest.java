/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.ObjectAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchTranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.search.ExperimentSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.server.api.v1.SearchCriteriaToDetailedSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.CompareMode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;

/**
 * @author pkupczyk
 */
public class ExperimentSearchCriteriaTranslatorTest extends AbstractSearchCriteriaTranslatorTest
{

    @Test
    public void testWithOrOperator()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));
        v1Criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY_2", "testValue_2"));
        v1Criteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withOrOperator();
        v3Criteria.withProperty("PROPERTY").thatEquals("testValue");
        v3Criteria.withProperty("PROPERTY_2").thatEquals("testValue_2");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithAndOperator()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));
        v1Criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY_2", "testValue_2"));
        v1Criteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withAndOperator();
        v3Criteria.withProperty("PROPERTY").thatEquals("testValue");
        v3Criteria.withProperty("PROPERTY_2").thatEquals("testValue_2");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithCode()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "testCode"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withCode().thatEquals("testCode");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithPermId()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "testPermId"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withPermId().thatEquals("testPermId");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithType()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "testType"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withType().withCode().thatEquals("testType");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithProject()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, "testProject"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withProject().withCode().thatEquals("testProject");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithSpace()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "testSpace"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withProject().withSpace().withCode().thatEquals("testSpace");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithTag()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.METAPROJECT, "testTag"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withTag().withCode().thatEquals("testTag");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithRegistrationDate()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.REGISTRATION_DATE, CompareMode.EQUALS, "2014-04-07",
                "0"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withRegistrationDate().thatEquals("2014-04-07");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithModificationDate()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.MODIFICATION_DATE, CompareMode.EQUALS, "2014-04-07",
                "0"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withModificationDate().thatEquals("2014-04-07");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithProperty()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withProperty("PROPERTY").thatEquals("testValue");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithAnyProperty()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAnyPropertyMatch("testValue"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withAnyProperty().thatEquals("testValue");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithAnyField()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAnyFieldMatch("testValue"));

        ExperimentSearchCriteria v3Criteria = new ExperimentSearchCriteria();
        v3Criteria.withAnyField().thatEquals("testValue");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    private void translateAndAssertEqual(SearchCriteria expected, ExperimentSearchCriteria actual)
    {
        DetailedSearchCriteria detailedExpected = translate(expected);
        DetailedSearchCriteria detailedActual = translate(actual);
        Assert.assertEquals(detailedExpected.toString(), detailedActual.toString());
    }

    private DetailedSearchCriteria translate(ExperimentSearchCriteria criteria)
    {
        ExperimentSearchCriteriaTranslator translator = new ExperimentSearchCriteriaTranslator(daoFactory, new ObjectAttributeProviderFactory());
        DetailedSearchCriteria result = translator.translate(new SearchTranslationContext(null), criteria).getCriteria();
        System.out.println("From NEW:\n" + result);
        return result;
    }

    private DetailedSearchCriteria translate(SearchCriteria criteria)
    {
        DetailedSearchCriteria result =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(daoFactory, SearchableEntityKind.EXPERIMENT, criteria);
        System.out.println("From OLD:\n" + result);
        return result;
    }

}
