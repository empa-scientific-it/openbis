/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * An <i>abstract</i> entity type property type.
 * 
 * @author Christian Ribeaud
 */
public abstract class EntityTypePropertyType<T extends EntityType> implements Serializable,
        Comparable<EntityTypePropertyType<T>>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private EntityKind entityKind;

    private boolean mandatory;

    private boolean managedInternally;

    private Long ordinal;

    private String section;

    private PropertyType propertyType;

    private T entityType;

    private boolean dynamic;

    private boolean managed;

    private boolean showInEditView;

    private boolean showRawValue;

    private Script script;

    private Date modificationDate;

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    public Script getScript()
    {
        return script;
    }

    public void setScript(Script script)
    {
        this.script = script;
    }

    public EntityTypePropertyType(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public final boolean isDynamic()
    {
        return dynamic;
    }

    public final void setDynamic(final boolean dynamic)
    {
        this.dynamic = dynamic;
    }

    public final boolean isManaged()
    {
        return managed;
    }

    public final void setManaged(final boolean managed)
    {
        this.managed = managed;
    }

    /**
     * Some properties, managed properties in particular, may or may not need to be available in edit and update views.
     * <p>
     * The current behavior is to always return true for non-script-based properties and always return false for dynamically computed properties. Only
     * dynamically managed properties take this value into account.
     * 
     * @return True if the property should be shown in edit and update views.
     */
    public final boolean isShownInEditView()
    {
        if (isDynamic())
        {
            return false;
        } else
        {
            return showInEditView;
        }
    }

    /**
     * Some properties, managed properties in particular, may or may not need to be available in edit and update views.
     * <p>
     * The current behavior is to always use the value true for non-script-based properties and always use false for dynamically computed properties.
     * Only dynamically managed properties take this value into account.
     * 
     * @param showInEditView Pass in true if this property should be shown in edit and update views.
     */
    public final void setShownInEditView(final boolean showInEditView)
    {
        // Just set the value, the semantics of how this is applicable to dynamic / simple
        // properties
        // is implemented in the getter.
        this.showInEditView = showInEditView;
    }

    public boolean getShowRawValue()
    {
        return showRawValue;
    }

    public void setShowRawValue(boolean showRawValue)
    {
        this.showRawValue = showRawValue;
    }

    public final boolean isMandatory()
    {
        return mandatory;
    }

    public final void setMandatory(final boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    public final boolean isManagedInternally()
    {
        return managedInternally;
    }

    public final void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    public final PropertyType getPropertyType()
    {
        return propertyType;
    }

    public final void setPropertyType(final PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    public final T getEntityType()
    {
        return entityType;
    }

    public final void setEntityType(final T entityType)
    {
        this.entityType = entityType;
    }

    public Long getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }

    public String getSection()
    {
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    //
    // Object
    //

    // NOTE: Equals and hashCode don't take into account entity type.

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof EntityTypePropertyType == false)
        {
            return false;
        }
        EntityTypePropertyType<T> that = (EntityTypePropertyType<T>) obj;
        return getPropertyType().equals(that.getPropertyType());
    }

    @Override
    public int hashCode()
    {
        return getPropertyType().hashCode();
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final EntityTypePropertyType<T> o)
    {
        assert o != null : "Unspecified entity type property type.";

        final EntityType entityType1 = getEntityType();
        final EntityType entityType2 = o.getEntityType();
        final Long ordinal1 = getOrdinal();
        final Long ordinal2 = o.getOrdinal();
        // first sort by entity type and then use ordinal information
        if (entityType1.equals(entityType2))
        {
            return ordinal1.compareTo(ordinal2);
        } else
        {
            return entityType1.compareTo(entityType2);
        }
    }
}
