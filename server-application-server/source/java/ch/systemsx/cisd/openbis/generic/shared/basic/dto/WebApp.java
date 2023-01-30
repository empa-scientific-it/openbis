/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * Information about a web application.
 * 
 * @author pkupczyk
 */
public class WebApp implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    private String label;

    private Integer sorting;

    private String[] contexts;

    private Map<EntityKind, String[]> entityTypes;

    // GWT
    @SuppressWarnings("unused")
    private WebApp()
    {
    }

    public WebApp(String code)
    {
        this(code, null, null, null, null);
    }

    public WebApp(String code, String label, Integer sorting, String[] contexts,
            Map<EntityKind, String[]> entityTypes)
    {
        if (code == null)
        {
            throw new IllegalArgumentException("Code cannot be null");
        }
        this.code = code;
        this.label = label;
        this.sorting = sorting;
        this.contexts = contexts;
        this.entityTypes = entityTypes;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public Integer getSorting()
    {
        return sorting;
    }

    public String[] getContexts()
    {
        return contexts;
    }

    public Map<EntityKind, String[]> getEntityTypes()
    {
        return entityTypes;
    }

    public boolean matchesContext(WebAppContext context)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        return matches(contexts, context.getName());
    }

    public boolean matchesEntity(EntityKind entityKind, BasicEntityType entityType)
    {
        if (entityKind == null)
        {
            throw new IllegalArgumentException("Entity kind cannot be null");
        }
        if (entityType == null)
        {
            throw new IllegalArgumentException("Entity type cannot be null");
        }

        if (entityTypes == null || entityTypes.get(entityKind) == null)
        {
            return false;
        } else
        {
            return matches(entityTypes.get(entityKind), entityType.getCode());
        }
    }

    private boolean matches(String[] configuredValues, String checkedValue)
    {
        if (configuredValues == null)
        {
            return false;
        }
        for (String configuredValue : configuredValues)
        {
            if (checkedValue.matches(configuredValue))
            {
                return true;
            }
        }
        return false;
    }

}