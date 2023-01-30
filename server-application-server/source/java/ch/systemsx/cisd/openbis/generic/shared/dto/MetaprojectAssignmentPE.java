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

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * @author Pawel Glyzewski
 */
@Entity
@Table(name = TableNames.METAPROJECT_ASSIGNMENTS_VIEW)
public class MetaprojectAssignmentPE implements Serializable, IIdHolder
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private MetaprojectPE metaproject;

    private ExperimentPE experiment;

    private SamplePE sample;

    private DataPE dataSet;

    private MaterialPE material;

    /**
     * Deletion information.
     * <p>
     * If not <code>null</code>, then this data set is considered <i>deleted</i> (moved to trash).
     * </p>
     */
    private DeletionPE deletion;

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.METAPROJECT_ASSIGNMENTS_SEQUENCE, sequenceName = SequenceNames.METAPROJECT_ASSIGNMENTS_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.METAPROJECT_ASSIGNMENTS_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.METAPROJECT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.METAPROJECT_ID_COLUMN, updatable = false)
    public MetaprojectPE getMetaproject()
    {
        return metaproject;
    }

    public void setMetaproject(MetaprojectPE metaproject)
    {
        this.metaproject = metaproject;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = false)
    public ExperimentPE getExperiment()
    {
        return experiment;
    }

    public void setExperiment(ExperimentPE experiment)
    {
        this.experiment = experiment;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SAMPLE_COLUMN, updatable = false)
    public SamplePE getSample()
    {
        return sample;
    }

    public void setSample(SamplePE sample)
    {
        this.sample = sample;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATA_ID_COLUMN, updatable = false)
    public DataPE getDataSet()
    {
        return dataSet;
    }

    public void setDataSet(DataPE dataSet)
    {
        this.dataSet = dataSet;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.MATERIAL_COLUMN, updatable = false)
    public MaterialPE getMaterial()
    {
        return material;
    }

    public void setMaterial(MaterialPE material)
    {
        this.material = material;
    }

    /**
     * Checks the type of entity and calls the appropriate setter (e.g. {@code setMaterial} )
     */
    public void setEntity(IEntityWithMetaprojects entity)
    {
        if (entity instanceof MaterialPE)
        {
            setMaterial((MaterialPE) entity);
        } else if (entity instanceof DataPE)
        {
            setDataSet((DataPE) entity);
        } else if (entity instanceof ExperimentPE)
        {
            setExperiment((ExperimentPE) entity);
        } else if (entity instanceof SamplePE)
        {
            setSample((SamplePE) entity);
        } else
        {
            throw new NullPointerException("Must specify entity to set.");
        }
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DELETION_COLUMN)
    public DeletionPE getDeletion()
    {
        return deletion;
    }

    public void setDeletion(final DeletionPE deletion)
    {
        this.deletion = deletion;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getMetaproject(), "metaproject");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof MetaprojectAssignmentPE == false)
        {
            return false;
        }
        final MetaprojectAssignmentPE that = (MetaprojectAssignmentPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getMetaproject(), that.getMetaproject());
        builder.append(getExperiment(), that.getExperiment());
        builder.append(getSample(), that.getSample());
        builder.append(getDataSet(), that.getDataSet());
        builder.append(getMaterial(), that.getMaterial());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getMetaproject());
        builder.append(getExperiment());
        builder.append(getSample());
        builder.append(getDataSet());
        builder.append(getMaterial());
        return builder.toHashCode();
    }
}
