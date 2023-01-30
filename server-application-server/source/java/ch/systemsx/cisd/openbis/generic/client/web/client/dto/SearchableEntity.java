/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A searchable entity.
 * 
 * @author Christian Ribeaud
 */
public final class SearchableEntity implements IsSerializable, Comparable<SearchableEntity>
{
    private String name;

    private String description;

    private Type type;

    private String possibleSearchOptionsKey;
    
    private List<SearchOption> possibleSearchOptions;

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    public final String getName()
    {
        return name;
    }

    public final void setName(final String name)
    {
        this.name = name;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public String getPossibleSearchOptionsKey()
    {
        return possibleSearchOptionsKey;
    }

    public void setPossibleSearchOptionsKey(String possibleSearchOptionsKey)
    {
        this.possibleSearchOptionsKey = possibleSearchOptionsKey;
    }
    
    public List<SearchOption> getPossibleSearchOptions()
    {
        return possibleSearchOptions;
    }
    
    public void setPossibleSearchOptions(List<SearchOption> possibleSearchOptions)
    {
        this.possibleSearchOptions = possibleSearchOptions;
    }

    @Override
    public final String toString()
    {
        return getDescription();
    }

    @Override
    public final int compareTo(final SearchableEntity o)
    {
        assert o != null : "Unspecified searchable entity";
        return getDescription().compareTo(o.getDescription());
    }
}
