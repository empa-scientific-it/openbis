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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Persistence Entity representing a deleted data set mapping only those attributes that are needed.
 * 
 * @author Piotr Buczek
 */
@Entity
@Table(name = TableNames.DELETED_DATA_VIEW)
@Inheritance(strategy = InheritanceType.JOINED)
public class DeletedDataPE extends AbstractDeletedEntityPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private Long experimentId;

    private Long sampleId;

    private DataStorePE dataStore;

    private DataSetTypePE dataSetType;

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.DATA_SEQUENCE, sequenceName = SequenceNames.DATA_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATA_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @Override
    @Transient
    public String getPermId()
    {
        return getCode();
    }

    @Column(name = ColumnNames.EXPERIMENT_COLUMN, nullable = false, insertable = false, updatable = false)
    public Long getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(final Long experimentId)
    {
        this.experimentId = experimentId;
    }

    @Column(name = ColumnNames.SAMPLE_COLUMN, nullable = false, insertable = false, updatable = false)
    public Long getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(final Long experimentId)
    {
        this.sampleId = experimentId;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof DeletedDataPE == false)
        {
            return false;
        }
        final DeletedDataPE that = (DeletedDataPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("deletion", getDeletion());
        return builder.toString();
    }

    /**
     * return true if the data set if available in the data store.
     */
    @Transient
    public boolean isAvailable()
    {
        return false;
    }

    /**
     * return true if the data set can be deleted.
     */
    @Transient
    public boolean isDeletable()
    {
        return true;
    }

    @Transient
    public DeletedExternalDataPE tryAsExternalData()
    {
        return (this instanceof DeletedExternalDataPE) ? (DeletedExternalDataPE) this : null;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.DATA_STORE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATA_STORE_COLUMN, updatable = false)
    public DataStorePE getDataStore()
    {
        return dataStore;
    }

    public void setDataStore(final DataStorePE dataStorePE)
    {
        this.dataStore = dataStorePE;
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        return getCode();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.DATA_SET_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATA_SET_TYPE_COLUMN)
    public DataSetTypePE getDataSetType()
    {
        return dataSetType;
    }

    /** Sets <code>dataSetType</code>. */
    public void setDataSetType(final DataSetTypePE dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    @Override
    @Transient
    public EntityTypePE getEntityType()
    {
        return getDataSetType();
    }

    @Override
    @Transient
    public EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

}
