/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RelationshipHistoryEntry extends EntityModification
{
    public final String type = "RELATIONSHIP";

    @JsonProperty("key")
    public String relationType;

    @JsonProperty("value")
    public String relatedEntity;

    public String entityType;

    @Override
    public String toString()
    {
        return "RelationshipHistoryEntry [permId=" + permId + ", relationType=" + relationType + ", relatedEntity="
                + relatedEntity + ", entityType=" + entityType + ", userId=" + userId + ", validFromTimestamp="
                + validFrom + ", validUntilTimestamp=" + validUntil + "]";
    }
}