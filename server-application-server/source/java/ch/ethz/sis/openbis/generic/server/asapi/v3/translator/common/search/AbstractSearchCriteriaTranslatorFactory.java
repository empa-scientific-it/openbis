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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchCriteriaTranslatorFactory
{

    private List<ISearchCriteriaTranslator> translators;

    public ISearchCriteriaTranslator getTranslator(ISearchCriteria criteria)
    {
        for (ISearchCriteriaTranslator translator : getTranslators())
        {
            if (translator.accepts(criteria))
            {
                return translator;
            }
        }

        throw new IllegalArgumentException("Could not find any translator that accepts criteria: " + criteria);
    }

    private List<ISearchCriteriaTranslator> getTranslators()
    {
        if (translators == null)
        {
            translators = createTranslators();
        }
        return translators;
    }

    protected abstract List<ISearchCriteriaTranslator> createTranslators();

}
