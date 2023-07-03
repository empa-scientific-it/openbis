/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.util.Date;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectBaseRecord;

/**
 * @author Franz-Josef Elmer
 */
public class PropertyTypeRecord extends ObjectBaseRecord
{
    public String code;

    public String label;

    public String description;

    public String data_type;

    public Boolean is_managed_internally;

    public String schema;

    public String transformation;

    public Date registration_timestamp;

    public String meta_data;

    public Boolean is_multi_value;

}
