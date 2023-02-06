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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.ObjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Identifies a sample by identifier.
 * 
 * @author pkupczyk
 */
@JsonObject("SampleIdentifierId")
public class SampleIdentifierId extends ObjectIdentifierId implements ISampleId
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /**
     * @param identifier Sample identifier, e.g. "/MY_SPACE/MY_SAMPLE" (space sample) or "/MY_SAMPLE" (shared sample)
     */
    public SampleIdentifierId(String identifier)
    {
        super(identifier);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private SampleIdentifierId()
    {
        super();
    }

}
