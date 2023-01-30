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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister;

import java.util.Date;

/**
 * @author pkupczyk
 */
public class DataSetRecord
{

    public Long ds_id;

    public String ds_code;

    public String ds_data_set_kind;

    public Date ds_registration_timestamp;

    public Date ds_modification_timestamp;

    public Boolean ds_is_post_registered;

    public Date access_timestamp;

    public Long ctnr_id;

    public String ctnr_code;

    public String dt_code;

    public Boolean ed_sc;

    public String ex_code;

    public String sa_code;

    public String sa_identifier;

    public String sac_code;

    public String pe_first_name;

    public String pe_last_name;

    public String pe_email;

    public String pe_user_id;

    public String mod_first_name;

    public String mod_last_name;

    public String mod_email;

    public String mod_user_id;

    public String pre_code;

    public String spe_code;

    public String sps_code;

    public Long die_id;

    public String die_code;

    public String die_uuid;

    public Boolean die_is_original_source;

    public String ld_external_code;

    public Long edms_id;

    public String edms_code;

    public String edms_label;

    public String edms_address;

    public String edms_address_type;

}
