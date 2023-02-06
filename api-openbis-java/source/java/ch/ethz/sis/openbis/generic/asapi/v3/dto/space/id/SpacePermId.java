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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Space perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.space.id.SpacePermId")
public class SpacePermId extends ObjectPermId implements ISpaceId
{

    private static final long serialVersionUID = 1L;

    /**
     * @param permId Space perm id, e.g. "/MY_SPACE" or "MY_SPACE".
     */
    public SpacePermId(String permId)
    {
        super(permId != null ? permId.toUpperCase() : null);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private SpacePermId()
    {
        super();
    }

    @Override
    public String getPermId()
    {
        String permId = super.getPermId();

        // support both "/MY_SPACE" and "MY_SPACE"
        if (permId.startsWith("/"))
        {
            return permId.substring(1);
        } else
        {
            return permId;
        }
    }

}
