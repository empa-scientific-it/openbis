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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * External data management system perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.externaldms.id.ExternalDmsPermId")
public class ExternalDmsPermId extends ObjectPermId implements IExternalDmsId
{

    private static final long serialVersionUID = 1L;

    /**
     * @param permId External data management system perm id, e.g. "DMS1".
     */
    public ExternalDmsPermId(String permId)
    {
        super(permId != null ? permId.toUpperCase() : null);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private ExternalDmsPermId()
    {
        super();
    }

}
