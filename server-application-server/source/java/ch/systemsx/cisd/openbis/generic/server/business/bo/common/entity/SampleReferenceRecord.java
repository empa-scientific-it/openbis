/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import ch.rinn.restrictions.Private;

/**
 * Basic information about a referenced sample.
 * 
 * @author Tomasz Pylak
 */
@Private
public class SampleReferenceRecord
{
    public long id;

    public String perm_id;

    /** sample code */
    public String s_code;

    /** deletion id */
    public Long del_id;

    /** sample type code */
    public String st_code;

    /** space code */
    public String spc_code;

    /** project tech id */
    public Long proj_id;
    
    /** project code */
    public String proj_code;
    
    /** project space code */
    public String proj_space_code;
    
    /** container code */
    public String c_code;
}
