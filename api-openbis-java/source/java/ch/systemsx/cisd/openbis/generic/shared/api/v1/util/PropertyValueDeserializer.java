/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.systemsx.cisd.openbis.generic.shared.api.v1.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class PropertyValueDeserializer extends JsonDeserializer<Serializable>
{
    @Override
    public Serializable deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException
    {
        JsonNode node = p.readValueAsTree();
        if(node.isArray()) {
            ArrayList<String> list = new ArrayList<>();
            node.forEach(value -> list.add(value.textValue()));
            return list.toArray(new String[0]);
        } else {
            return node.textValue();
        }
    }


}
