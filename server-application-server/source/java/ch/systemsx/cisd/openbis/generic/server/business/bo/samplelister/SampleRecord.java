/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import java.util.Date;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;

/**
 * A record object representing one row of the sample table.
 */
// CREATE TABLE SAMPLES (
// ID TECH_ID NOT NULL,
// PERM_ID CODE NOT NULL,
// CODE CODE NOT NULL,
// EXPE_ID TECH_ID,
// SATY_ID TECH_ID NOT NULL,
// REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,
// MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP,
// PERS_ID_REGISTERER TECH_ID NOT NULL,
// del_id TECH_ID,
// DBIN_ID TECH_ID,
// SPACE_ID TECH_ID,
// SAMP_ID_PART_OF TECH_ID);
@Private
public class SampleRecord extends CodeRecord
{
    public String perm_id;

    public Long expe_id;

    public Long proj_id;

    public Long space_id;

    public Long samp_id_part_of;

    public Date registration_timestamp;

    public Date modification_timestamp;

    public Long pers_id_registerer;

    public Long pers_id_modifier;

    public Long del_id;

    public long saty_id;

    public int version;
}
