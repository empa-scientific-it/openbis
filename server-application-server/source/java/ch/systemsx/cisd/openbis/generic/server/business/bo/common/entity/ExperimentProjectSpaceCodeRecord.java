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
package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import ch.rinn.restrictions.Private;

/**
 * A class representing an experiment, project and space code. It contains also deletion id, code of experiment type and database instance id.
 */
@Private
public class ExperimentProjectSpaceCodeRecord
{
    public long id;

    public String e_code;

    public String e_permid;

    public String et_code;

    public String p_code;

    public String spc_code;

    public Long p_id;

    public String p_perm_id;

    public Long dbin_id;

    public Long del_id;

}
