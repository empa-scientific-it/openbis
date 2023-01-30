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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.Location;

/**
 * {@link ExternalDataPE} counterpart for deleted data sets mapping only those attributes that are needed for permanent deletion of data sets.
 * 
 * @author Piotr Buczek
 */
@Entity
@Table(name = TableNames.EXTERNAL_DATA_TABLE, uniqueConstraints = @UniqueConstraint(columnNames = { ColumnNames.LOCATION_COLUMN,
        ColumnNames.LOCATOR_TYPE_COLUMN }))
@PrimaryKeyJoinColumn(name = ColumnNames.ID_COLUMN)
public final class DeletedExternalDataPE extends DeletedDataPE
{
    private static final long serialVersionUID = IServer.VERSION;

    // minimal set of fields needed for permanent deletion of data set

    private String location;

    private DataSetArchivingStatus status = DataSetArchivingStatus.AVAILABLE;

    private boolean isPresentInArchive;

    /** Returns <code>locator</code>. */
    @Column(name = ColumnNames.LOCATION_COLUMN)
    @Length(max = 1024, message = ValidationMessages.LOCATION_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.LOCATION_NOT_NULL_MESSAGE)
    @Location(relative = true, message = ValidationMessages.LOCATION_NOT_RELATIVE)
    public String getLocation()
    {
        return location;
    }

    /** Sets <code>locator</code>. */
    public void setLocation(final String location)
    {
        this.location = location;
    }

    @NotNull(message = ValidationMessages.STATUS_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.STATUS)
    @Enumerated(EnumType.STRING)
    public DataSetArchivingStatus getStatus()
    {
        return status;
    }

    public void setStatus(DataSetArchivingStatus status)
    {
        this.status = status;
    }

    @Column(name = ColumnNames.PRESENT_IN_ARCHIVE)
    public boolean isPresentInArchive()
    {
        return isPresentInArchive;
    }

    public void setPresentInArchive(boolean isPresentInArchive)
    {
        this.isPresentInArchive = isPresentInArchive;
    }

    /**
     * return true if the data set if available in the data store.
     */
    @Transient
    @Override
    public boolean isAvailable()
    {
        return getStatus().isAvailable();
    }

    /**
     * return true if the data set can be deleted.
     */
    @Transient
    @Override
    public boolean isDeletable()
    {
        return getStatus().isDeletable();
    }

}
