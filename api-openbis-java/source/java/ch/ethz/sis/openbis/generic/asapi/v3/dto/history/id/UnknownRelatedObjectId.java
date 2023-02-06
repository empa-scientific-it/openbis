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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.history.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonObject("as.dto.history.id.UnknownRelatedObjectId")
public class UnknownRelatedObjectId implements IObjectId
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String relatedObjectId;

    @JsonProperty
    private String relationType;

    public UnknownRelatedObjectId(String relatedObjectId, String relationType)
    {
        this.relatedObjectId = relatedObjectId;
        this.relationType = relationType;
    }

    public String getRelatedObjectId()
    {
        return relatedObjectId;
    }

    public String getRelationType()
    {
        return relationType;
    }

    //
    // JSON-RPC
    //

    protected UnknownRelatedObjectId()
    {
    }

    @Override
    public String toString()
    {
        return getRelatedObjectId() + " (" + getRelationType() + ")";
    }

}
