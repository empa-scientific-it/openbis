/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.common.parser.BeanProperty;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * The full description of a new sample component.
 * <p>
 * Used when registering a sample component to the LIMS.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class NewSampleComponent extends NewSampleComponentWithFixedMaterialType
{
    private static final long serialVersionUID = IServer.VERSION;

    @SuppressWarnings("hiding")
    public final static NewSampleComponent[] EMPTY_ARRAY = new NewSampleComponent[0];

    private String materialTypeCode;

    public final String getMaterialTypeCode()
    {
        return materialTypeCode;
    }

    @BeanProperty(label = "material_type")
    public final void setMaterialTypeCode(final String materialTypeCode)
    {
        this.materialTypeCode = materialTypeCode;
    }
}
