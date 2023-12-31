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
package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import java.util.Date;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;

/**
 * A record object representing one row of the dataset table.
 */
@Private
public class DatasetRecord extends CodeRecord
{
    // --- from data table

    public long dsty_id;

    public long dast_id;

    public Long expe_id;

    public String data_producer_code;

    public Date production_timestamp;

    public Long samp_id;

    public Date registration_timestamp;

    public Date modification_timestamp;

    public Date access_timestamp;

    public int version;

    public Long pers_id_registerer;

    public Long pers_id_modifier;

    public boolean is_valid;

    public boolean is_derived;

    public Long del_id;

    public String data_set_kind;

    // ---- from external_data table
    // ---- can be NULL in case of container(virtual) data sets

    public String share_id;

    public String location;

    public Long size;

    public Long ffty_id;

    public Long loty_id;

    public String is_complete; // maps to BooleanOrUnknown

    public String status;

    public Boolean present_in_archive;

    public Integer speed_hint;

    public Boolean storage_confirmation;

    public Boolean is_post_registered;

    public Boolean h5_folders;

    public Boolean h5ar_folders;

    public Boolean archiving_requested;
}
