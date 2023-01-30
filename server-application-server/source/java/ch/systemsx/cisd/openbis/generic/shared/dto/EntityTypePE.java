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

import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Persistence Entity representing entity type.
 * <p>
 * Entity is one of: material, sample, experiment
 * </p>
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
@MappedSuperclass
public abstract class EntityTypePE extends AbstractTypePE
{
    private static final long serialVersionUID = IServer.VERSION;

    @Transient
    public abstract EntityKind getEntityKind();

    private Date modificationDate;

    private ScriptPE validationScript;

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.VALIDATION_SCRIPT_ID_COLUMN, updatable = true)
    public ScriptPE getValidationScript()
    {
        return validationScript;
    }

    public void setValidationScript(final ScriptPE validationScript)
    {
        this.validationScript = validationScript;
    }

    @Transient
    public abstract Collection<? extends EntityTypePropertyTypePE> getEntityTypePropertyTypes();

    /**
     * Creates an {@link EntityPropertyPE} from given <var>entityKind</var>.
     */
    public final static <T extends EntityTypePE> T createEntityTypePE(final EntityKind entityKind)
    {
        assert entityKind != null : "Unspecified entity kind.";
        return ClassUtils.createInstance(entityKind.<T> getTypeClass());
    }

    //
    // AbstractTypePE
    //

    @Override
    ToStringBuilder createStringBuilder()
    {
        final ToStringBuilder builder = super.createStringBuilder();
        return builder;
    }

    //
    // Object
    //

    @Override
    public boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityTypePE == false)
        {
            return false;
        }
        final EntityTypePE that = (EntityTypePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getEntityKind(), that.getEntityKind());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getEntityKind());
        return builder.toHashCode();
    }
}
