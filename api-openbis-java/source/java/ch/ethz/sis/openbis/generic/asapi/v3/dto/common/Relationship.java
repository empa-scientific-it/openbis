/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.common.Relationship")
public class Relationship implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Map<String, String> childAnnotations;

    private Map<String, String> parentAnnotations;

    public Map<String, String> getChildAnnotations()
    {
        return childAnnotations;
    }

    public void setChildAnnotations(Map<String, String> childAnnotations)
    {
        this.childAnnotations = childAnnotations;
    }

    public Map<String, String> getParentAnnotations()
    {
        return parentAnnotations;
    }

    public void setParentAnnotations(Map<String, String> parentAnnotations)
    {
        this.parentAnnotations = parentAnnotations;
    }

    @JsonIgnore
    public Relationship addChildAnnotation(String key, String value)
    {
        if (childAnnotations == null)
        {
            childAnnotations = new HashMap<>();
        }
        childAnnotations.put(key, value);
        return this;
    }

    @JsonIgnore
    public Relationship addParentAnnotation(String key, String value)
    {
        if (parentAnnotations == null)
        {
            parentAnnotations = new HashMap<>();
        }
        parentAnnotations.put(key, value);
        return this;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("parent annotations", parentAnnotations)
                .append("child annotations", childAnnotations).toString();
    }
}
