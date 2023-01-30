package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Use this criteria class if you want to enable auto-refresh when property types change. Even when there are no criteria to pre-filter the grid
 * besides the standard paging tab controls.
 */
public class PropertyTypesCriteria extends DefaultResultSetConfig<String, AbstractExternalData>
{
    private List<PropertyType> propertyTypesOrNull;

    public List<PropertyType> tryGetPropertyTypes()
    {
        return propertyTypesOrNull;
    }

    public void setPropertyTypes(List<PropertyType> propertyTypes)
    {
        this.propertyTypesOrNull = propertyTypes;
    }
}
