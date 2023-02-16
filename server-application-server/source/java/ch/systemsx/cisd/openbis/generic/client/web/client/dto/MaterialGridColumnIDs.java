/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;

/**
 * Columns of a grid which shows {@link Material} rows.
 * 
 * @author Tomasz Pylak
 */
public class MaterialGridColumnIDs
{
    public static final String CODE = "CODE";

    public static final String REGISTRATOR = "REGISTRATOR";

    public static final String REGISTRATION_DATE = "REGISTRATION_DATE";

    public static final String MATERIAL_TYPE = "MATERIAL_TYPE";

    public static final String METAPROJECTS = "METAPROJECTS";

    public static final String PROPERTIES_GROUP = "property-";

}
