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
 * Persistence entity representing a historical sample.
 * 
 * @author Franz-Josef Elmer
 */
@Entity
@Table(name = TableNames.SAMPLE_HISTORY_VIEW)
public class SampleHistoryPE extends AbstractEntityHistoryPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private Long sampleId;

    private Long dataSetId;

    private Long spaceId;

    private Long experimentId;

    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = SamplePE.class)
    @JoinColumn(name = ColumnNames.MAIN_SAMPLE_COLUMN)
    SamplePE getEntityInternal()
    {
        return (SamplePE) entity;
    }

    @SuppressWarnings("unused")
    private void setEntityInternal(SamplePE sample)
    {
        entity = sample;
    }

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = SampleTypePropertyTypePE.class)
    @JoinColumn(name = ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN)
    public EntityTypePropertyTypePE getEntityTypePropertyTypeInternal()
    {
        return entityTypePropertyType;
    }

    @SuppressWarnings("unused")
    private void setEntityTypePropertyTypeInternal(SampleTypePropertyTypePE sampleTypePropertyType)
    {
        entityTypePropertyType = sampleTypePropertyType;
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

    @Column(name = ColumnNames.SPACE_COLUMN)
    public Long getSpaceId()
    {
        return spaceId;
    }

    public void setSpaceId(Long spaceId)
    {
        this.spaceId = spaceId;
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

    @Column(name = ColumnNames.PROJECT_COLUMN)
    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
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
        } else if (spaceId != null)
        {
            return new RelatedSpace(spaceId);
        } else if (experimentId != null)
        {
            return new RelatedExperiment(experimentId);
        } else if (projectId != null)
        {
            return new RelatedProject(projectId);
        }
        return null;
    }
}
