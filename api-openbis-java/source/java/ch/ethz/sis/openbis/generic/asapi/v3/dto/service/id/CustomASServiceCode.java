/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Custom AS service code. This is the name of an AS core plugin of type 'services'.
 * 
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.service.id.CustomASServiceCode")
public class CustomASServiceCode extends ObjectPermId implements ICustomASServiceId
{
    private static final long serialVersionUID = 1L;

    public CustomASServiceCode(String code)
    {
        // case sensitive
        super(code);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private CustomASServiceCode()
    {
    }
}
