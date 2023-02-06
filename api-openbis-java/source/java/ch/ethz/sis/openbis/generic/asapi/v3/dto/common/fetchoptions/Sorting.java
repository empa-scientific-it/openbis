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
import java.util.Map;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.fetchoptions.Sorting")
public class Sorting implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String field;

    private SortOrder order;
    
    private Map<SortParameter, String> parameters;
    
    @SuppressWarnings("unused")
    private Sorting()
    {
    }

    public Sorting(String field, SortOrder order)
    {
        this(field, order, null);
    }
    
    public Sorting(String field, SortOrder order, Map<SortParameter, String> parameters)
    {
        this.field = field;
        this.order = order;
        this.parameters = parameters;
    }

    public String getField()
    {
        return field;
    }

    public SortOrder getOrder()
    {
        return order;
    }
    
    public Map<SortParameter, String> getParameters() {
    		return parameters;
    }

    @Override
    public String toString()
    {
        return "order by " + field + " " + order;
    }
}
