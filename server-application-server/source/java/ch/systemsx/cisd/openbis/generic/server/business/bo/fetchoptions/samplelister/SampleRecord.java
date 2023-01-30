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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaprojectRecord;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;

/**
 * @author Franz-Josef Elmer
 */
public class SampleRecord
{

    public Long s_id;

    public String s_perm_id;

    public String s_code;

    public String container_code;

    public Date s_registration_timestamp;

    public Date s_modification_timestamp;

    public Long st_id;

    public String st_code;

    public String sp_code;

    public String pe_first_name;

    public String pe_last_name;

    public String pe_user_id;

    public String pe_email;

    public String mod_first_name;

    public String mod_last_name;

    public String mod_user_id;

    public String mod_email;

    public String exp_code;

    public String proj_code;

    public String proj_space_code;
    
    public String samp_proj_code;
    
    public List<SampleRecord> children;

    public List<SampleRecord> parents;

    public Map<String, String> properties;

    public List<MetaprojectRecord> metaprojects;

    public EnumSet<SampleFetchOption> fetchOptions;

}
