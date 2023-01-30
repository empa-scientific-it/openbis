package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

public class NewPTNewAssigment implements Serializable
{
    private boolean existingPropertyType;

    private PropertyType propertyType;

    private NewETPTAssignment assignment;

    public boolean isExistingPropertyType()
    {
        return existingPropertyType;
    }

    public void setExistingPropertyType(boolean existingPropertyType)
    {
        this.existingPropertyType = existingPropertyType;
    }

    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    public NewETPTAssignment getAssignment()
    {
        return assignment;
    }

    public void setAssignment(NewETPTAssignment assignment)
    {
        this.assignment = assignment;
    }

}