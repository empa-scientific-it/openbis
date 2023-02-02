/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * @author Franz-Josef Elmer
 */
public class Constants
{
    public static final String IMAGE_VIEWER_LAUNCH_SERVLET_NAME =
            GenericConstants.createServicePath(ScreeningConstants.IMAGE_VIEWER_LAUNCH_URL);

    public static final String MATERIAL_DETAILS_PROPERTY_TYPE_KEY =
            "material-details-property-type";
}
