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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A <i>Persistence Entity</i> which represents a group.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.SPACES_TABLE, uniqueConstraints = { @UniqueConstraint(columnNames = { ColumnNames.CODE_COLUMN }) })
public final class SpacePE extends HibernateAbstractRegistrationHolder implements IIdAndCodeHolder, IIdentityHolder,
        Comparable<SpacePE>, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final SpacePE[] EMPTY_ARRAY = new SpacePE[0];

    private transient Long id;

    private String code;

    private boolean frozen;

    private boolean frozenForProject;

    private boolean frozenForSample;

    private String description;

    // null if unknown
    private Boolean home;

    private Date modificationDate;

    public final void setCode(final String code)
    {
        this.code = code;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @Override
    @Transient
    public String getPermId()
    {
        return code;
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        return new SpaceIdentifier(code).toString();
    }

    @Transient
    public final Boolean isHome()
    {
        return home;
    }

    public final void setHome(final Boolean home)
    {
        this.home = home;
    }

    @NotNull
    @Column(name = ColumnNames.FROZEN_COLUMN, nullable = false)
    public boolean isFrozen()
    {
        return frozen;
    }

    public void setFrozen(boolean frozen)
    {
        this.frozen = frozen;
    }

    @NotNull
    @Column(name = ColumnNames.FROZEN_FOR_PROJECT_COLUMN, nullable = false)
    public boolean isFrozenForProject()
    {
        return frozenForProject;
    }

    public void setFrozenForProject(boolean frozenForProject)
    {
        this.frozenForProject = frozenForProject;
    }

    @NotNull
    @Column(name = ColumnNames.FROZEN_FOR_SAMPLE_COLUMN, nullable = false)
    public boolean isFrozenForSample()
    {
        return frozenForSample;
    }

    public void setFrozenForSample(boolean frozenForSample)
    {
        this.frozenForSample = frozenForSample;
    }

    @Version
    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    //
    // IIdAndCodeHolder
    //

    @Override
    @SequenceGenerator(name = SequenceNames.SPACE_SEQUENCE, sequenceName = SequenceNames.SPACE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SPACE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Override
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public final String getCode()
    {
        return code;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SpacePE == false)
        {
            return false;
        }
        final SpacePE that = (SpacePE) obj;
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
        builder.append("description", getDescription());
        builder.append("home", isHome());
        return builder.toString();
    }

    //
    // Compare
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    @Override
    public final int compareTo(final SpacePE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

    //
    // connected projects and samples are functional only for fetching (no bidirectional support for connection)
    //

    private List<ProjectPE> projects = new ArrayList<ProjectPE>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "space")
    public List<ProjectPE> getProjects()
    {
        return projects;
    }

    @SuppressWarnings("unused")
    private void setProjects(List<ProjectPE> projects)
    {
        this.projects = projects;
    }

    private List<SamplePE> samples = new ArrayList<SamplePE>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "space")
    public List<SamplePE> getSamples()
    {
        return samples;
    }

    @SuppressWarnings("unused")
    private void setSamples(List<SamplePE> samples)
    {
        this.samples = samples;
    }

}
