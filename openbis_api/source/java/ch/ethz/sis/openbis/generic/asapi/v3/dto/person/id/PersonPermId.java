/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Person perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.person.id.PersonPermId")
public class PersonPermId extends ObjectPermId implements IPersonId
{

    private static final long serialVersionUID = 1L;

    /**
     * @param permId Person perm id, e.g. "admin".
     */
    public PersonPermId(String permId)
    {
        // case sensitive
        super(permId);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private PersonPermId()
    {
        super();
    }

}
