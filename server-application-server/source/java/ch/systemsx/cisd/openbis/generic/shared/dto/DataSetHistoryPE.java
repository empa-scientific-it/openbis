/*
 * Copyright 2012 ETH Zuerich, CISD
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Persistence entity representing a historical data set property.
 * 
 * @author Franz-Josef Elmer
 */
@Entity
@Table(name = TableNames.DATA_SET_HISTORY_VIEW)
public class DataSetHistoryPE extends AbstractEntityHistoryPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private Long sampleId;

    private Long dataSetId;

    private Long experimentId;

    private Long externalDmsId;

    private String externalDmsCode;

    private String externalDmsLabel;

    private String externalDmsAddress;

    private String externalCode;

    private String path;

    private String gitCommitHash;

    private String gitRepositoryId;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DataPE.class)
    @JoinColumn(name = ColumnNames.MAIN_DATA_SET_COLUMN)
    DataPE getEntityInternal()
    {
        return (DataPE) entity;
    }

    @SuppressWarnings("unused")
    private void setEntityInternal(DataPE dataSet)
    {
        entity = dataSet;
    }

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = DataSetTypePropertyTypePE.class)
    @JoinColumn(name = ColumnNames.DATA_SET_TYPE_PROPERTY_TYPE_COLUMN)
    public EntityTypePropertyTypePE getEntityTypePropertyTypeInternal()
    {
        return entityTypePropertyType;
    }

    @SuppressWarnings("unused")
    private void setEntityTypePropertyTypeInternal(DataSetTypePropertyTypePE dataSetTypePropertyType)
    {
        entityTypePropertyType = dataSetTypePropertyType;
    }

    @Column(name = ColumnNames.SAMPLE_COLUMN)
    public Long getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(Long sampleId)
    {
        this.sampleId = sampleId;
    }

    @Column(name = ColumnNames.DATA_ID_COLUMN)
    public Long getDataSetId()
    {
        return dataSetId;
    }

    public void setDataSetId(Long dataSetId)
    {
        this.dataSetId = dataSetId;
    }

    @Column(name = ColumnNames.EXPERIMENT_COLUMN)
    public Long getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(Long experimentId)
    {
        this.experimentId = experimentId;
    }

    @Column(name = ColumnNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_ID_COLUMN)
    public Long getExternalDmsId()
    {
        return externalDmsId;
    }

    public void setExternalDmsId(Long externalDmsId)
    {
        this.externalDmsId = externalDmsId;
    }

    @Column(name = ColumnNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_CODE_COLUMN)
    public String getExternalDmsCode()
    {
        return externalDmsCode;
    }

    public void setExternalDmsCode(String externalDmsCode)
    {
        this.externalDmsCode = externalDmsCode;
    }

    @Column(name = ColumnNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_LABEL_COLUMN)
    public String getExternalDmsLabel()
    {
        return externalDmsLabel;
    }

    public void setExternalDmsLabel(String externalDmsLabel)
    {
        this.externalDmsLabel = externalDmsLabel;
    }

    @Column(name = ColumnNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_ADDRESS_COLUMN)
    public String getExternalDmsAddress()
    {
        return externalDmsAddress;
    }

    public void setExternalDmsAddress(String externalDmsAddress)
    {
        this.externalDmsAddress = externalDmsAddress;
    }

    @Column(name = ColumnNames.EXTERNAL_CODE_COLUMN)
    public String getExternalCode()
    {
        return externalCode;
    }

    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    @Column(name = ColumnNames.PATH_COLUMN)
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    @Column(name = ColumnNames.GIT_COMMIT_HASH_COLUMN)
    public String getGitCommitHash()
    {
        return gitCommitHash;
    }

    public void setGitCommitHash(String gitCommitHash)
    {
        this.gitCommitHash = gitCommitHash;
    }

    @Column(name = ColumnNames.GIT_REPOSITORY_ID_COLUMN)
    public String getGitRepositoryId()
    {
        return gitRepositoryId;
    }

    public void setGitRepositoryId(String gitRepositoryId)
    {
        this.gitRepositoryId = gitRepositoryId;
    }

    @Override
    @Transient
    public IRelatedEntity getRelatedEntity()
    {
        if (sampleId != null)
        {
            return new RelatedSample(sampleId);
        } else if (dataSetId != null)
        {
            return new RelatedDataSet(dataSetId);
        } else if (experimentId != null)
        {
            return new RelatedExperiment(experimentId);
        } else if (externalDmsId != null)
        {
            return new RelatedExternalDms(externalDmsId);
        }
        return null;
    }

}
