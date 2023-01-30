/*
 * Copyright 2016 ETH Zuerich, SIS
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

/**
 * @author Franz-Josef Elmer
 */
public class AttributeEntry extends EntityModification
{
    public static final String ATTRIBUTE = "ATTRIBUTE";

    public final String type = ATTRIBUTE;

    @JsonProperty("key")
    public String attributeName;

    public String value;

    @Override
    public String toString()
    {
        return "AttributeEntry [permId=" + permId + ", attributeName=" + attributeName + ", value="
                + value + "]";
    }
}
