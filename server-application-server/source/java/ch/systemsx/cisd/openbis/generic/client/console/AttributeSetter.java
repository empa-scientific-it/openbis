package ch.systemsx.cisd.openbis.generic.client.console;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * @author Pawel Glyzewski
 */
public interface AttributeSetter<T extends EntityType>
{
    public String getAttributeName();

    public void setAttributeFor(T object, String value);

    public void setDefaultFor(T object);
}