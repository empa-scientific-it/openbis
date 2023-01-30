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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.ObjectAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchTranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.search.SampleSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.server.api.v1.SearchCriteriaToDetailedSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.CompareMode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;

/**
 * @author pkupczyk
 */
public class SampleSearchCriteriaTranslatorTest extends AbstractSearchCriteriaTranslatorTest
{

    @Test
    public void testWithOrOperator()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));
        v1Criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY_2", "testValue_2"));
        v1Criteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
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

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
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

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withCode().thatEquals("testCode");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithPermId()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "testPermId"));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withPermId().thatEquals("testPermId");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithType()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "testType"));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withType().withCode().thatEquals("testType");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithSpace()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "testSpace"));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withSpace().withCode().thatEquals("testSpace");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithTag()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.METAPROJECT, "testTag"));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withTag().withCode().thatEquals("testTag");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithParents()
    {
        SearchCriteria subCriteria = new SearchCriteria();
        subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "parentCode"));

        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(subCriteria));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withParents().withCode().thatEquals("parentCode");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithChildren()
    {
        SearchCriteria subCriteria = new SearchCriteria();
        subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "childCode"));

        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(subCriteria));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withChildren().withCode().thatEquals("childCode");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithContainer()
    {
        SearchCriteria subCriteria = new SearchCriteria();
        subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "containerCode"));

        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(subCriteria));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withContainer().withCode().thatEquals("containerCode");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithRegistrationDate()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.REGISTRATION_DATE, CompareMode.EQUALS, "2014-04-07",
                "0"));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withRegistrationDate().thatEquals("2014-04-07");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithModificationDate()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.MODIFICATION_DATE, CompareMode.EQUALS, "2014-04-07",
                "0"));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withModificationDate().thatEquals("2014-04-07");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithProperty()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "testValue"));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withProperty("PROPERTY").thatEquals("testValue");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithAnyProperty()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAnyPropertyMatch("testValue"));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withAnyProperty().thatEquals("testValue");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithAnyField()
    {
        SearchCriteria v1Criteria = new SearchCriteria();
        v1Criteria.addMatchClause(MatchClause.createAnyFieldMatch("testValue"));

        SampleSearchCriteria v3Criteria = new SampleSearchCriteria();
        v3Criteria.withAnyField().thatEquals("testValue");

        translateAndAssertEqual(v1Criteria, v3Criteria);
    }

    @Test
    public void testWithMany()
    {
        SearchCriteria experimentCriteria = new SearchCriteria();
        experimentCriteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "experimentCode"));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, "projectCode"));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "spaceCode"));

        SearchCriteria v1SampleCriteria = new SearchCriteria();
        v1SampleCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "sampleType"));
        v1SampleCriteria.addMatchClause(MatchClause.createPropertyMatch("PROPERTY", "sampleValue"));
        v1SampleCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentCriteria));
        v1SampleCriteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);

        SampleSearchCriteria v3SampleCriteria = new SampleSearchCriteria();
        v3SampleCriteria.withAndOperator();
        v3SampleCriteria.withType().withCode().thatEquals("sampleType");
        v3SampleCriteria.withProperty("PROPERTY").thatEquals("sampleValue");

        ExperimentSearchCriteria v3ExperimentCriteria = v3SampleCriteria.withExperiment();
        v3ExperimentCriteria.withCode().thatEquals("experimentCode");

        ProjectSearchCriteria v3ProjectCriteria = v3ExperimentCriteria.withProject();
        v3ProjectCriteria.withCode().thatEquals("projectCode");
        v3ProjectCriteria.withSpace().withCode().thatEquals("spaceCode");

        translateAndAssertEqual(v1SampleCriteria, v3SampleCriteria);
    }

    private void translateAndAssertEqual(SearchCriteria expected, SampleSearchCriteria actual)
    {
        DetailedSearchCriteria detailedExpected = translate(expected);
        DetailedSearchCriteria detailedActual = translate(actual);
        Assert.assertEquals(detailedExpected.toString(), detailedActual.toString());
    }

    private DetailedSearchCriteria translate(SampleSearchCriteria criteria)
    {
        SampleSearchCriteriaTranslator translator = new SampleSearchCriteriaTranslator(daoFactory, new ObjectAttributeProviderFactory());
        DetailedSearchCriteria result = translator.translate(new SearchTranslationContext(null), criteria).getCriteria();
        System.out.println("From NEW:\n" + result);
        return result;
    }

    private DetailedSearchCriteria translate(SearchCriteria criteria)
    {
        DetailedSearchCriteria result = SearchCriteriaToDetailedSearchCriteriaTranslator.convert(daoFactory, SearchableEntityKind.SAMPLE, criteria);
        System.out.println("From OLD:\n" + result);
        return result;
    }

}
