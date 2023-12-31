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

public class PropertyHistoryEntry extends EntityModification
{
    public final String type = "PROPERTY";

    @JsonProperty("key")
    public String propertyCode;

    public String value;

    @Override
    public String toString()
    {
        return "HistoryPropertyEntry [permId=" + permId + ", propertyCode=" + propertyCode + ", value=" + value
                + ", user_id=" + userId
                + ", valid_from_timestamp=" + validFrom + ", valid_until_timestamp=" + validUntil + "]";
    }
}
