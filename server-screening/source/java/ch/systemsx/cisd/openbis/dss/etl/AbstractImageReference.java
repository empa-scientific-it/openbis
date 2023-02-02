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

package ch.systemsx.cisd.openbis.dss.etl;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Superclass for image reference.
 * 
 * @author Tomasz Pylak
 */
public class AbstractImageReference extends AbstractHashable
{
    private final String imageIdOrNull;

    private final ColorComponent colorComponentOrNull;

    public AbstractImageReference(String imageIdOrNull, ColorComponent colorComponentOrNull)
    {
        this.imageIdOrNull = imageIdOrNull;
        this.colorComponentOrNull = colorComponentOrNull;
    }

    public String tryGetImageID()
    {
        return imageIdOrNull;
    }

    public ColorComponent tryGetColorComponent()
    {
        return colorComponentOrNull;
    }

}
