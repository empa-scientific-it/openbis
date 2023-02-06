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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Plugin (i.e. dynamic property evaluator, managed property handler, entity validator) perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.plugin.id.PluginPermId")
public class PluginPermId extends ObjectPermId implements IPluginId
{

    private static final long serialVersionUID = 1L;

    /**
     * @param permId Plugin perm id, e.g. "MY_PLUGIN". Plugin perm id is case sensitive.
     */
    public PluginPermId(String permId)
    {
        // plugin perm id is case sensitive
        super(permId);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private PluginPermId()
    {
        super();
    }

}
