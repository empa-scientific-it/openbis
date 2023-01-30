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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Persistence Entity representing deleted experiment.
 * 
 * @author Piotr Buczek
 */
@Entity
@Table(name = TableNames.DELETED_EXPERIMENTS_VIEW)
public class DeletedExperimentPE extends AbstractDeletedEntityPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private String permIdInternal;

    private ExperimentIdentifier experimentIdentifier;

    private ProjectPE project;

    private ExperimentTypePE experimentType;

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.EXPERIMENT_SEQUENCE, sequenceName = SequenceNames.EXPERIMENT_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EXPERIMENT_SEQUENCE)
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

    @Override
    @Transient
    public final EntityTypePE getEntityType()
    {
        return getExperimentType();
    }

    @Override
    @Transient
    public final EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.EXPERIMENT_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.EXPERIMENT_TYPE_COLUMN, updatable = false)
    public ExperimentTypePE getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(final ExperimentTypePE experimentType)
    {

        this.experimentType = experimentType;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.PROJECT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.PROJECT_COLUMN, updatable = false)
    private ProjectPE getProjectInternal()
    {
        return project;
    }

    @Private
    public void setProjectInternal(final ProjectPE project)
    {
        this.project = project;
    }

    @Transient
    public ProjectPE getProject()
    {
        return getProjectInternal();
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        if (experimentIdentifier == null)
        {
            experimentIdentifier =
                    new ExperimentIdentifier(getProject().getIdentifier(), getCode());
        }
        return experimentIdentifier.toString();
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
        if (obj instanceof DeletedExperimentPE == false)
        {
            return false;
        }
        final DeletedExperimentPE that = (DeletedExperimentPE) obj;
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
