/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Relation between a {@link EntityTypePE} of something and a {@link PropertyTypePE}.
 * <p>
 * This represents an entry in <code>{entity}_type_property_types</code> table.
 * </p>
 * 
 * @author Franz-Josef Elmer
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
@MappedSuperclass
public abstract class EntityTypePropertyTypePE extends HibernateAbstractRegistrationHolder
        implements IEntityTypePropertyType, Comparable<EntityTypePropertyTypePE>
{
    private static final long serialVersionUID = IServer.VERSION;

    private boolean mandatory;

    private boolean managedInternally;

    private Long ordinal;

    private String section;

    protected transient Long id;

    protected EntityTypePE entityType;

    private PropertyTypePE propertyType;

    private ScriptPE script;

    private boolean shownInEditView;

    private boolean showRawValue;

    private Date modificationDate;

    private boolean unique;

    final public static <T extends EntityTypePropertyTypePE> T createEntityTypePropertyType(
            final EntityKind entityKind)
    {
        return ClassUtils.createInstance(entityKind.<T> getEntityTypePropertyTypeAssignmentClass());
    }

    @Version
    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @NotNull(message = ValidationMessages.PROPERTY_TYPE_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PROPERTY_TYPE_COLUMN, updatable = false)
    private PropertyTypePE getPropertyTypeInternal()
    {
        return propertyType;
    }

    @Transient
    public PropertyTypePE getPropertyType()
    {
        return getPropertyTypeInternal();
    }

    void setPropertyTypeInternal(final PropertyTypePE propertyType)
    {
        this.propertyType = propertyType;
    }

    abstract public void setPropertyType(final PropertyTypePE propertyType);

    @Transient
    public boolean isScriptable()
    {
        return getScript() != null;
    }

    @Transient
    public boolean isDynamic()
    {
        return isScriptable() && getScript().isDynamic();
    }

    @Transient
    public boolean isManaged()
    {
        return isScriptable() && getScript().isManaged();
    }

    @NotNull
    @Column(name = ColumnNames.IS_SHOWN_EDIT, updatable = true)
    public boolean isShownInEditView()
    {
        return shownInEditView;
    }

    public void setShownInEditView(final boolean shownInEditView)
    {
        this.shownInEditView = shownInEditView;
    }

    @NotNull
    @Column(name = ColumnNames.SHOW_RAW_VALUE, updatable = true)
    public boolean getShowRawValue()
    {
        return showRawValue;
    }

    public void setShowRawValue(final boolean showRawValue)
    {
        this.showRawValue = showRawValue;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SCRIPT_ID_COLUMN, updatable = true)
    public ScriptPE getScript()
    {
        return script;
    }

    public void setScript(final ScriptPE script)
    {
        this.script = script;
    }

    @NotNull
    @Column(name = ColumnNames.IS_MANDATORY, updatable = true)
    public boolean isMandatory()
    {
        return mandatory;
    }

    public void setMandatory(final boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    @NotNull
    @Column(name = ColumnNames.IS_MANAGED_INTERNALLY)
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    @Column(name = ColumnNames.ORDINAL_COLUMN)
    @NotNull(message = ValidationMessages.ORDINAL_NOT_NULL_MESSAGE)
    public Long getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }

    @Column(name = ColumnNames.SECTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.SECTION_LENGTH_MESSAGE)
    public String getSection()
    {
        return section;
    }

    @NotNull
    @Column(name = ColumnNames.IS_UNIQUE, updatable = false)
    public boolean isUnique()
    {
        return unique;
    }

    public void setUnique(final boolean unique)
    {
        this.unique = unique;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    // needed by Hibernate, must match the mapped getter name
    void setEntityTypeInternal(final EntityTypePE entityType)
    {
        this.entityType = entityType;
    }

    public void setEntityType(final EntityTypePE entityType)
    {
        setEntityTypeInternal(entityType);
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("managedInternally", isManagedInternally());
        builder.append("mandatory", isMandatory());
        builder.append("propertyType", getPropertyType());
        builder.append("entityType", getEntityType());
        builder.append("ordinal", getOrdinal());
        builder.append("section", getSection());
        builder.append("dynamic", isDynamic());
        builder.append("managed", isManaged());
        builder.append("unique", isUnique());
        return builder.toString();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityTypePropertyTypePE == false)
        {
            return false;
        }
        final EntityTypePropertyTypePE that = (EntityTypePropertyTypePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getPropertyType(), that.getPropertyType());
        builder.append(getEntityType(), that.getEntityType());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getPropertyType());
        builder.append(getEntityType());
        return builder.toHashCode();
    }

    //
    // Comparable
    //

    @Override
    public int compareTo(EntityTypePropertyTypePE o)
    {
        return this.getOrdinal().compareTo(o.getOrdinal());
    }

}
