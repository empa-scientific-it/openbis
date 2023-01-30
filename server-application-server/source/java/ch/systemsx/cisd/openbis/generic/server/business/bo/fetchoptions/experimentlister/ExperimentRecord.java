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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.experimentlister;

import java.util.Date;

/**
 * @author pkupczyk
 */
public class ExperimentRecord
{

    public Long e_id;

    public String e_perm_id;

    public String e_code;

    public Date e_registration_timestamp;

    public Date e_modification_timestamp;

    public Long et_id;

    public String et_code;

    public String et_description;

    public Long s_id;

    public String s_code;

    public String s_description;

    public Date s_registration_timestamp;

    public Long pr_id;

    public String pr_perm_id;

    public String pr_code;

    public Date pr_registration_timestamp;

    public String pr_description;

    public Date pr_modification_timestamp;

    public Long pe_id;

    public String pe_first_name;

    public String pe_last_name;

    public String pe_user_id;

    public String pe_email;

    public Date pe_registration_timestamp;

    public String mod_first_name;

    public String mod_last_name;

    public String mod_user_id;

    public String mod_email;

    public Date mod_registration_timestamp;
}
