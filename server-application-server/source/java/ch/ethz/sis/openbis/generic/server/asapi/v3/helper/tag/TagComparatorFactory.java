/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.tag;

import java.util.Comparator;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagSortOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.CodeComparator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.RegistrationDateComparator;

/**
 * @author pkupczyk
 */
public class TagComparatorFactory extends ComparatorFactory
{

    @Override
    public boolean accepts(Class<?> sortOptionsClass)
    {
        return TagSortOptions.class.isAssignableFrom(sortOptionsClass);
    }

    @Override
    public Comparator<Tag> getComparator(String field, Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        if (TagSortOptions.CODE.equals(field))
        {
            return new CodeComparator<Tag>();
        } else if (TagSortOptions.REGISTRATION_DATE.equals(field))
        {
            return new RegistrationDateComparator<Tag>();
        } else
        {
            return null;
        }
    }

    @Override
    public Comparator<Tag> getDefaultComparator()
    {
        return new CodeComparator<Tag>();
    }

}
