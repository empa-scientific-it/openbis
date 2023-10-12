/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Custom DSS service code. This is the name of an DS core plugin of type 'services'.
 *
 */
@JsonObject("dss.dto.service.id.CustomDssServiceCode")
public class CustomDssServiceCode extends ObjectPermId implements ICustomDSSServiceId
{
    private static final long serialVersionUID = 1L;

    public CustomDssServiceCode(String code)
    {
        super(code);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private CustomDssServiceCode()
    {
    }
}
