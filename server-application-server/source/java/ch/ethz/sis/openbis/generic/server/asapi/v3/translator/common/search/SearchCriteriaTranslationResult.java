/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search;

import java.util.Arrays;
import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;

/**
 * @author pkupczyk
 */
public class SearchCriteriaTranslationResult
{

    private DetailedSearchCriterion[] criterionList = new DetailedSearchCriterion[] {};

    private DetailedSearchSubCriteria[] subCriteriaList = new DetailedSearchSubCriteria[] {};

    private DetailedSearchCriteria[] criteriaList = new DetailedSearchCriteria[] {};

    public SearchCriteriaTranslationResult(DetailedSearchCriterion criterion)
    {
        this.criterionList = new DetailedSearchCriterion[] { criterion };
    }

    public SearchCriteriaTranslationResult(DetailedSearchCriterion... criterionList)
    {
        this.criterionList = criterionList;
    }

    public SearchCriteriaTranslationResult(DetailedSearchSubCriteria criteria)
    {
        this.subCriteriaList = new DetailedSearchSubCriteria[] { criteria };
    }

    public SearchCriteriaTranslationResult(DetailedSearchSubCriteria... criteriaList)
    {
        this.subCriteriaList = criteriaList;
    }

    public SearchCriteriaTranslationResult(DetailedSearchCriteria criteria)
    {
        this.criteriaList = new DetailedSearchCriteria[] { criteria };
    }

    public SearchCriteriaTranslationResult(DetailedSearchCriteria... criteriaList)
    {
        this.criteriaList = criteriaList;
    }

    public Collection<? extends DetailedSearchCriterion> getCriterionList()
    {
        return Arrays.asList(criterionList);
    }

    public DetailedSearchCriterion getCriterion()
    {
        if (criterionList.length != 1)
        {
            throw new IllegalStateException("Does not have one element: " + Arrays.toString(criterionList));
        }
        return criterionList[0];
    }

    public Collection<? extends DetailedSearchSubCriteria> getSubCriteriaList()
    {
        return Arrays.asList(subCriteriaList);
    }

    public DetailedSearchSubCriteria getSubCriteria()
    {
        if (subCriteriaList.length != 1)
        {
            throw new IllegalStateException("Does not have one element: " + Arrays.toString(subCriteriaList));
        }
        return subCriteriaList[0];
    }

    public Collection<? extends DetailedSearchCriteria> getCriteriaList()
    {
        return Arrays.asList(criteriaList);
    }

    public DetailedSearchCriteria getCriteria()
    {
        if (criteriaList.length != 1)
        {
            throw new IllegalStateException("Does not have one element: " + Arrays.toString(criteriaList));
        }
        return criteriaList[0];
    }

}
