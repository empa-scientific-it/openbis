/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;

/**
 * Services offered by the Data Store server to the public.<br>
 * A <i>Persistence Entity</i> which represents an entry in {@link TableNames#DATA_STORE_SERVICES_TABLE}.
 * 
 * @author Tomasz Pylak
 */
@Entity
@Table(name = TableNames.DATA_STORE_SERVICES_TABLE)
@Friend(toClasses = DataStorePE.class)
public class DataStoreServicePE implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private String key;

    private String label;

    private DataStoreServiceKind kind;

    private ReportingPluginType reportingPluginTypeOrNull;

    private DataStorePE dataStore;

    // which types of datasets can be processed by this plugin?
    private Set<DataSetTypePE> datasetTypes;

    @Id
    @SequenceGenerator(name = SequenceNames.DATA_STORE_SERVICE_SEQUENCE, sequenceName = SequenceNames.DATA_STORE_SERVICE_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATA_STORE_SERVICE_SEQUENCE)
    private final Long getId()
    {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    @Column(nullable = false, name = ColumnNames.DATA_STORE_SERVICE_KEY_COLUMN)
    @NotNull(message = ValidationMessages.IDENTIFIER_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 256, message = ValidationMessages.IDENTIFIER_LENGTH_MESSAGE)
    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    @Column(nullable = false, name = ColumnNames.DATA_STORE_SERVICE_LABEL_COLUMN)
    @NotNull(message = ValidationMessages.LABEL_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 256, message = ValidationMessages.LABEL_LENGTH_MESSAGE)
    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinTable(name = TableNames.DATA_STORE_SERVICES_DATASET_TYPES_TABLE, joinColumns = @JoinColumn(name = ColumnNames.DATA_STORE_SERVICES_DATASET_TYPES_PARENT_COLUMN), inverseJoinColumns = @JoinColumn(name = ColumnNames.DATA_STORE_SERVICES_DATASET_TYPES_CHILDREN_COLUMN))
    public Set<DataSetTypePE> getDatasetTypes()
    {
        return datasetTypes;
    }

    public void setDatasetTypes(Set<DataSetTypePE> datasetTypes)
    {
        this.datasetTypes = datasetTypes;
    }

    @NotNull(message = ValidationMessages.DATA_STORE_SERVICE_KIND_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.DATA_STORE_SERVICE_KIND_COLUMN)
    @Enumerated(EnumType.STRING)
    public DataStoreServiceKind getKind()
    {
        return kind;
    }

    public void setKind(DataStoreServiceKind kind)
    {
        this.kind = kind;
    }

    @Column(name = ColumnNames.DATA_STORE_SERVICE_REPORTING_PLUGIN_TYPE)
    @Enumerated(EnumType.STRING)
    public ReportingPluginType getReportingPluginTypeOrNull()
    {
        return reportingPluginTypeOrNull;
    }

    public void setReportingPluginTypeOrNull(ReportingPluginType reportingPluginType)
    {
        this.reportingPluginTypeOrNull = reportingPluginType;
    }

    @NotNull(message = ValidationMessages.DATA_STORE_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.DATA_STORE_SERVICE_PARENT_COLUMN)
    private DataStorePE getDataStoreInternal()
    {
        return dataStore;
    }

    @Private
    void setDataStoreInternal(DataStorePE dataStore)
    {
        this.dataStore = dataStore;
    }

    @Transient
    public DataStorePE getDataStore()
    {
        return getDataStoreInternal();
    }

    public void setDataStore(DataStorePE dataStore)
    {
        assert dataStore != null;
        dataStore.addService(this);
    }

    @Transient
    public boolean isTableReport()
    {
        return DataStoreServiceKind.QUERIES.equals(getKind()) && ReportingPluginType.TABLE_MODEL.equals(getReportingPluginTypeOrNull());
    }
}
