/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;

/**
 * IDisplayTypeIDGenerator specific to screening.
 * 
 * @author Izabela Adamczyk
 */
public enum ScreeningDisplayTypeIDGenerator implements IDisplayTypeIDGenerator
{
    PLATE_METADATA_GRID("plate-metadata-grid"),

    EXPERIMENT_CHANNEL("experiment-channel"),

    MATERIAL_DISAMBIGUATION_GRID("material-disambiguation-grid");

    private final String genericNameOrPrefix;

    private ScreeningDisplayTypeIDGenerator(String genericNameOrPrefix)
    {
        this.genericNameOrPrefix = genericNameOrPrefix;
    }

    @Override
    public String createID()
    {
        return genericNameOrPrefix;
    }

    @Override
    public String createID(String suffix)
    {
        return genericNameOrPrefix + suffix;
    }

}
