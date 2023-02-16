/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes the material update operation, currently only properties can be changed.
 * 
 * @author Tomasz Pylak
 */
public class MaterialUpdateDTO extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final TechId materialId;

    private final List<IEntityProperty> properties;

    private String[] metaprojectsOrNull;

    private final Date version;

    public MaterialUpdateDTO(TechId materialId, List<IEntityProperty> properties, Date version)
    {
        this.materialId = materialId;
        this.properties = properties;
        this.version = version;
    }

    public TechId getMaterialId()
    {
        return materialId;
    }

    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    public String[] getMetaprojectsOrNull()
    {
        return metaprojectsOrNull;
    }

    public void setMetaprojectsOrNull(String[] metaprojectsOrNull)
    {
        this.metaprojectsOrNull = metaprojectsOrNull;
    }

    public Date getVersion()
    {
        return version;
    }
}