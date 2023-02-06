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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.fetchoptions.SortOptions")
public abstract class SortOptions<OBJECT> implements Serializable
{

    private static final long serialVersionUID = 1L;

    private List<Sorting> sortings = new LinkedList<>();

    protected SortOrder getOrCreateSorting(String field)
    {
        return getOrCreateSortingWithParameters(field, null);
    }

    protected SortOrder getOrCreateSortingWithParameters(String field, Map<SortParameter, String> parameters)
    {
        SortOrder order = getSorting(field);
        if (order == null)
        {
            order = new SortOrder();
            sortings.add(new Sorting(field, order, parameters));
        }
        return order;
    }

    protected SortOrder getSorting(String field)
    {
        for (Sorting sorting : sortings)
        {
            if (field.equals(sorting.getField()))
            {
                return sorting.getOrder();
            }
        }
        return null;
    }

    public List<Sorting> getSortings()
    {
        return sortings;
    }

    @Override
    public String toString()
    {
        List<String> strings = new ArrayList<>();
        for (Sorting sort : sortings)
        {
            strings.add(sort.toString());
        }
        return StringUtils.join(strings, ", ");
    }

}
