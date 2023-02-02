/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.screening.shared.api.v1;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * This class exists only for backward compatibility. Please use
 * {@link ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration} instead.
 * 
 * @author pkupczyk
 */
@JsonObject("LoadImageConfigurationDss")
@Deprecated
public class LoadImageConfiguration extends
        ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration
{

    private static final long serialVersionUID = 1L;

}