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
public class PropertyAssignmentRecord extends ObjectBaseRecord
{

    public String section;

    public Integer ordinal;

    public Long prty_id;

    public String prty_code;

    public boolean prty_is_managed_internally;

    public Long type_id;

    public String type_code;

    public String kind_code;

    public Boolean is_mandatory;

    public Boolean is_shown_edit;

    public Boolean show_raw_value;

    public Long pers_id_registerer;

    public Date registration_timestamp;

    public Long script_id;

    public Boolean is_unique;
}
