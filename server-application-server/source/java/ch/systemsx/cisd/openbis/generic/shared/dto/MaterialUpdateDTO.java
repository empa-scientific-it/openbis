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