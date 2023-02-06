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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.Relationship;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.common.update.RelationshipUpdate")
public class RelationshipUpdate implements Serializable
{
    private static final long serialVersionUID = 1L;

    private ListUpdateMapValues childAnnotations = new ListUpdateMapValues();
    private ListUpdateMapValues parentAnnotations = new ListUpdateMapValues();

    @JsonIgnore
    public RelationshipUpdate addChildAnnotation(String key, String value)
    {
        childAnnotations.put(key, value);
        return this;
    }

    @JsonIgnore
    public RelationshipUpdate removeChildAnnotations(String... keys)
    {
        childAnnotations.remove(keys);
        return this;
    }

    @JsonIgnore
    public RelationshipUpdate addParentAnnotation(String key, String value)
    {
        parentAnnotations.put(key, value);
        return this;
    }

    @JsonIgnore
    public RelationshipUpdate removeParentAnnotations(String... keys)
    {
        parentAnnotations.remove(keys);
        return this;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public void setRelationship(Relationship relationship)
    {
        childAnnotations.set(relationship.getChildAnnotations());
        parentAnnotations.set(relationship.getParentAnnotations());
    }
    
    public ListUpdateMapValues getChildAnnotations()
    {
        return childAnnotations;
    }

    public void setChildAnnotations(ListUpdateMapValues childAnnotations)
    {
        this.childAnnotations = childAnnotations;
    }

    public ListUpdateMapValues getParentAnnotations()
    {
        return parentAnnotations;
    }

    public void setParentAnnotations(ListUpdateMapValues parentAnnotations)
    {
        this.parentAnnotations = parentAnnotations;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("child annotations update", childAnnotations)
                .append("parent annotations update", parentAnnotations).toString();
    }
}
