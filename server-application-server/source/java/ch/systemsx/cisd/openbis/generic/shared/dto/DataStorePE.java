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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * A <i>Persistence Entity</i> which represents an entry in {@link TableNames#DATA_STORES_TABLE}.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.DATA_STORES_TABLE)
@Friend(toClasses = DataStoreServicePE.class)
public final class DataStorePE extends AbstractIdAndCodeHolder<DataStorePE>
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    /**
     * The code of this data store.
     */
    private String code;

    /** Registration date of this data store. */
    private Date registrationDate;

    private String downloadUrl;

    private String remoteUrl;

    private String sessionToken;

    private String databaseInstanceUUID;

    private Date modificationDate;

    private boolean archiverConfigured;

    private Set<DataStoreServicePE> services = new HashSet<DataStoreServicePE>();

    private String serializedDataSourceDefinitions;

    public final void setId(final Long id)
    {
        this.id = id;
    }

    public final void setCode(final String code)
    {
        this.code = code;
    }

    @Column(name = ColumnNames.DOWNLOAD_URL_COLUMN, updatable = true)
    @NotNull(message = ValidationMessages.DOWNLOAD_URL_NOT_NULL_MESSAGE)
    public final String getDownloadUrl()
    {
        return downloadUrl;
    }

    public final void setDownloadUrl(final String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    @Column(name = ColumnNames.REMOTE_URL_COLUMN, updatable = true)
    @NotNull(message = ValidationMessages.REMOTE_URL_NOT_NULL_MESSAGE)
    public final String getRemoteUrl()
    {
        return remoteUrl;
    }

    public final void setRemoteUrl(String remoteUrl)
    {
        this.remoteUrl = remoteUrl;
    }

    @Column(name = ColumnNames.SESSION_TOKEN_COLUMN, updatable = true)
    @NotNull(message = ValidationMessages.SESSION_TOKEN_NOT_NULL_MESSAGE)
    public final String getSessionToken()
    {
        return sessionToken;
    }

    public final void setSessionToken(String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public final Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

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

    @Column(name = ColumnNames.UUID_COLUMN, nullable = false)
    public String getDatabaseInstanceUUID()
    {
        return databaseInstanceUUID;
    }

    public void setDatabaseInstanceUUID(final String databaseInstanceUUID)
    {
        this.databaseInstanceUUID = databaseInstanceUUID;
    }

    @NotNull
    @Column(name = ColumnNames.IS_ARCHIVER_CONFIGURED, updatable = true)
    public boolean isArchiverConfigured()
    {
        return archiverConfigured;
    }

    public void setArchiverConfigured(final boolean archiverConfigured)
    {
        this.archiverConfigured = archiverConfigured;
    }

    @Column(name = ColumnNames.DATA_SOURCE_DEFINITIONS, updatable = true)
    public String getSerializedDataSourceDefinitions()
    {
        return serializedDataSourceDefinitions;
    }

    public void setSerializedDataSourceDefinitions(String serializedDataSourceDefinitions)
    {
        this.serializedDataSourceDefinitions = serializedDataSourceDefinitions;
    }

    //
    // AbstractIdAndCodeHolder
    //

    @Override
    @Column(name = ColumnNames.CODE_COLUMN)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public final String getCode()
    {
        return code;
    }

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.DATA_STORE_SEQUENCE, sequenceName = SequenceNames.DATA_STORE_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATA_STORE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataStoreInternal", orphanRemoval = true)
    @NotNull(message = ValidationMessages.DATA_STORE_SERVICES_NOT_NULL_MESSAGE)
    private Set<DataStoreServicePE> getServicesInternal()
    {
        return services;
    }

    @SuppressWarnings("unused")
    // for Hibernate only
    private void setServicesInternal(Set<DataStoreServicePE> services)
    {
        this.services = services;
    }

    public void setServices(Set<DataStoreServicePE> services)
    {
        getServicesInternal().clear();
        for (DataStoreServicePE service : services)
        {
            addService(service);
        }
    }

    @Private
    void addService(DataStoreServicePE service)
    {
        DataStorePE dataStore = service.getDataStore();
        if (dataStore != null)
        {
            dataStore.getServicesInternal().remove(service);
        }
        service.setDataStoreInternal(this);
        getServicesInternal().add(service);
    }

    @Transient
    public Set<DataStoreServicePE> getServices()
    {
        return getServicesInternal();
    }
}
