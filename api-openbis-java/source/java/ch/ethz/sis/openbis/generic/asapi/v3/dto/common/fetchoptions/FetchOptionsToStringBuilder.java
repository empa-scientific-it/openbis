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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * @author jakubs
 */
@JsonIgnoreType
public class FetchOptionsToStringBuilder implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name;

    private FetchOptions<?> mainFetchOptions;

    private HashMap<String, FetchOptions<?>> childrenFetchOptions;

    private List<String> keys;

    public FetchOptionsToStringBuilder(String name, FetchOptions<?> fetchOptions)
    {
        this.name = name;
        this.mainFetchOptions = fetchOptions;
        this.childrenFetchOptions = new HashMap<>();
        this.keys = new ArrayList<>();
    }

    public FetchOptionsToStringBuilder addFetchOption(String key, FetchOptions<?> fo)
    {
        childrenFetchOptions.put(key, fo);
        keys.add(key);
        return this;
    }

    public String toString(String anIndentation, Set<FetchOptions<?>> processed)
    {
        StringBuilder sb = new StringBuilder();
        String indentation = anIndentation;

        if (indentation.isEmpty())
        {
            if (this.mainFetchOptions.getSortBy() != null)
            {
                sb.append(this.name + " " + this.mainFetchOptions.getSortBy() + "\n");
            } else
            {
                sb.append(this.name + "\n");
            }
        }

        processed.add(this.mainFetchOptions);

        indentation += "    ";

        for (String key : keys)
        {
            FetchOptions<?> subOptions = childrenFetchOptions.get(key);
            if (subOptions != null)
            {
                String sortOptionsPart = "";
                if (subOptions.getSortBy() != null)
                {
                    sortOptionsPart = " " + subOptions.getSortBy();
                }

                FetchOptionsToStringBuilder sbb = subOptions.getFetchOptionsStringBuilder();
                if (processed.contains(subOptions))
                {
                    sb.append(indentation + "with " + key + sortOptionsPart + "(recursive)\n");
                } else
                {
                    sb.append(indentation + "with " + key + sortOptionsPart + "\n");
                    sb.append(sbb.toString(indentation, processed));
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return toString("", new HashSet<FetchOptions<?>>());
    }

}
