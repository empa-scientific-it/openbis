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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.structured;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.EntityLinkElementKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityLinkElement;

/**
 * For now we only expose methods for creation of Sample and Material links, but it is quite easy to add support for experiment and datasets links in
 * the future.
 * 
 * @author Kaloyan Enimanev
 */
public class ElementFactory implements IElementFactory
{

    @Override
    public IElement createElement(String name)
    {
        if (EntityLinkElementKind.tryGetForElementName(name) != null)
        {
            String error = String.format("The name %s is reseved for link elements.", name);
            throw new IllegalArgumentException(error);
        }
        return new Element(name);
    }

    @Override
    public IEntityLinkElement createSampleLink(String permId)
    {
        return new EntityLinkElement(EntityLinkElementKind.SAMPLE, permId);
    }

    @Override
    public IEntityLinkElement createExperimentLink(String permId)
    {
        return new EntityLinkElement(EntityLinkElementKind.EXPERIMENT, permId);
    }

    @Override
    public IEntityLinkElement createDataSetLink(String permId)
    {
        return new EntityLinkElement(EntityLinkElementKind.DATA_SET, permId);
    }

    @Override
    public IEntityLinkElement createMaterialLink(String code, String typeCode)
    {
        String materialPermId = MaterialIdentifier.print(code, typeCode);
        return new EntityLinkElement(EntityLinkElementKind.MATERIAL, materialPermId);
    }

    @Override
    public boolean isEntityLink(IElement element)
    {
        return element instanceof IEntityLinkElement;
    }

}
