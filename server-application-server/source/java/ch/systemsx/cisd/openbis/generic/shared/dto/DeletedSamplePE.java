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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Persistence Entity representing deleted sample.
 * 
 * @author Piotr Buczek
 */
@Entity
@Table(name = TableNames.DELETED_SAMPLES_VIEW)
public class DeletedSamplePE extends AbstractDeletedEntityPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private Long containerId;

    private Long experimentId;

    private String permIdInternal;

    private SampleIdentifier sampleIdentifier;

    private SampleTypePE sampleType;

    private SpacePE space;

    private ProjectPE project;
    
    private Set<DeletedSampleRelationshipPE> parentRelationships;

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.SAMPLE_SEQUENCE, sequenceName = SequenceNames.SAMPLE_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
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

    @Column(name = ColumnNames.PART_OF_SAMPLE_COLUMN, nullable = false, insertable = false, updatable = false)
    public Long getContainerId()
    {
        return containerId;
    }

    public void setContainerId(final Long containerId)
    {
        this.containerId = containerId;
    }

    @Override
    @Transient
    public String getPermId()
    {
        return getPermIdInternal();
    }

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Column(name = ColumnNames.PERM_ID_COLUMN, nullable = false)
    private String getPermIdInternal()
    {
        return permIdInternal;
    }

    void setPermIdInternal(String permIdInternal)
    {
        this.permIdInternal = permIdInternal;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.SAMPLE_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.SAMPLE_TYPE_COLUMN, updatable = false)
    public SampleTypePE getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleTypePE sampleType)
    {
        this.sampleType = sampleType;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SPACE_COLUMN, updatable = false)
    public SpacePE getSpace()
    {
        return space;
    }

    public void setSpace(final SpacePE space)
    {
        this.space = space;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PROJECT_COLUMN, updatable = false)
    public ProjectPE getProject()
    {
        return project;
    }
    
    public void setProject(final ProjectPE project)
    {
        this.project = project;
    }
    
    @Override
    @Transient
    public EntityTypePE getEntityType()
    {
        return getSampleType();
    }

    @Override
    @Transient
    public EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        if (sampleIdentifier == null)
        {
            sampleIdentifier = IdentifierHelper.createSampleIdentifier(this);
        }
        return sampleIdentifier.toString();
    }

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.CHILD_SAMPLE_COLUMN)
    @Fetch(FetchMode.SUBSELECT)
    Set<DeletedSampleRelationshipPE> getParentRelationships()
    {
        return parentRelationships;
    }

    void setParentRelationships(Set<DeletedSampleRelationshipPE> parentRelationships)
    {
        this.parentRelationships = parentRelationships;
    }

    @Transient
    public List<Long> getParents()
    {
        if (parentRelationships == null || parentRelationships.isEmpty())
        {
            return Collections.emptyList();
        }
        List<Long> parentIds = new ArrayList<Long>();
        for (DeletedSampleRelationshipPE relationship : parentRelationships)
        {
            parentIds.add(relationship.getParentId());
        }
        return parentIds;
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
        if (obj instanceof DeletedSamplePE == false)
        {
            return false;
        }
        final DeletedSamplePE that = (DeletedSamplePE) obj;
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
}
