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
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.UnmodifiableListDecorator;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * A <i>Persistence Entity</i> which represents a project.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.PROJECTS_TABLE, uniqueConstraints = {
        @UniqueConstraint(columnNames = { ColumnNames.CODE_COLUMN, ColumnNames.SPACE_COLUMN }) })
@Friend(toClasses = { ExperimentPE.class, SamplePE.class })
public final class ProjectPE extends AttachmentHolderPE implements Comparable<ProjectPE>,
        IIdAndCodeHolder, IModifierAndModificationDateBean, IIdentityHolder, Serializable
{
    public static final ProjectPE[] EMPTY_ARRAY = new ProjectPE[0];

    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private String permId;

    private boolean frozen;

    private boolean frozenForExperiment;

    private boolean frozenForSample;

    private SpacePE space;

    private boolean spaceFrozen;

    private List<ExperimentPE> experiments = new ArrayList<ExperimentPE>();

    private String code;

    private String description;

    private PersonPE projectLeader;

    /** The number of experiments this project contains. */
    private int size;

    private PersonPE registrator;

    private PersonPE modifier;

    private Date registrationDate;

    private ProjectIdentifier projectIdentifier;

    private Date modificationDate;

    private int version;

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_REGISTERER_COLUMN, updatable = false)
    public PersonPE getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }

    @Override
    @OptimisticLock(excluded = true)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_MODIFIER_COLUMN)
    public PersonPE getModifier()
    {
        return modifier;
    }

    @Override
    public void setModifier(final PersonPE modifier)
    {
        this.modifier = modifier;
    }

    public final void setCode(final String code)
    {
        this.code = code;
    }

    /**
     * Sets the space which this <code>ProjectDTO</code> is related to.
     */
    public final void setSpace(final SpacePE space)
    {
        this.space = space;
        if (space != null)
        {
            spaceFrozen = space.isFrozen() && space.isFrozenForProject();
        }
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.SPACE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.SPACE_COLUMN, updatable = true)
    public final SpacePE getSpace()
    {
        return space;
    }

    @NotNull
    @Column(name = ColumnNames.SPACE_FROZEN_COLUMN, nullable = false)
    public boolean isSpaceFrozen()
    {
        if (space != null)
        {
            spaceFrozen = space.isFrozen() && space.isFrozenForProject();
        }
        return spaceFrozen;
    }

    public void setSpaceFrozen(boolean spaceFrozen)
    {
        this.spaceFrozen = spaceFrozen;
    }

    @OptimisticLock(excluded = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectInternal")
    private List<ExperimentPE> getExperimentsInternal()
    {
        return experiments;
    }

    // hibernate only
    @SuppressWarnings("unused")
    private void setExperimentsInternal(List<ExperimentPE> experiments)
    {
        this.experiments = experiments;
    }

    @Transient
    /* Note: modifications of the returned collection will result in an exception. */
    public List<ExperimentPE> getExperiments()
    {
        return new UnmodifiableListDecorator<ExperimentPE>(getExperimentsInternal());
    }

    public void setExperiments(List<ExperimentPE> experiments)
    {
        getExperimentsInternal().clear();
        for (ExperimentPE experiment : experiments)
        {
            addExperiment(experiment);
        }
    }

    @Private
    void addExperiment(ExperimentPE experiment)
    {
        ProjectPE project = experiment.getProject();
        if (project != null)
        {
            project.getExperimentsInternal().remove(experiment);
        }
        experiment.setProjectInternal(this);
        getExperimentsInternal().add(experiment);
    }

    @Transient
    public final int getSize()
    {
        return size;
    }

    public final void setSize(final int size)
    {
        this.size = size;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_LEADER_COLUMN, updatable = false)
    public final PersonPE getProjectLeader()
    {
        return projectLeader;
    }

    public final void setProjectLeader(final PersonPE projectLeader)
    {
        this.projectLeader = projectLeader;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_500000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        EqualsHashUtils.assertDefined(getSpace(), "space");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ProjectPE == false)
        {
            return false;
        }
        final ProjectPE that = (ProjectPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getSpace(), that.getSpace());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getSpace());
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
        return builder.toString();
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    @Override
    public final int compareTo(final ProjectPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

    //
    // IIdHolder
    //

    @Override
    @SequenceGenerator(name = SequenceNames.PROJECT_SEQUENCE, sequenceName = SequenceNames.PROJECT_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.PROJECT_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Override
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Column(name = ColumnNames.PERM_ID_COLUMN, nullable = false)
    public String getPermId()
    {
        return permId;
    }

    @Override
    @Column(unique = true)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public final String getCode()
    {
        return code;
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
    @Column(name = ColumnNames.FROZEN_FOR_EXPERIMENT_COLUMN, nullable = false)
    public boolean isFrozenForExperiment()
    {
        return frozenForExperiment;
    }

    public void setFrozenForExperiment(boolean frozenForExperiment)
    {
        this.frozenForExperiment = frozenForExperiment;
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

    @Override
    @Transient
    public AttachmentHolderKind getAttachmentHolderKind()
    {
        return AttachmentHolderKind.PROJECT;
    }

    @Override
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectParentInternal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    protected Set<AttachmentPE> getInternalAttachments()
    {
        return attachments;
    }

    @Override
    @Transient
    public final String getIdentifier()
    {
        if (projectIdentifier == null)
        {
            projectIdentifier = IdentifierHelper.createProjectIdentifier(this);
        }
        return projectIdentifier.toString();
    }

    @Override
    @OptimisticLock(excluded = true)
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    @Override
    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @Version
    @Column(name = ColumnNames.VERSION_COLUMN, nullable = false)
    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    /*
     * Non hibernate related methods to keep API compatibility inside the business logic
     */
    private static List<SamplePE> getProjectSamples(long projectTechId) {
        ISampleDAO sampleDAO = CommonServiceProvider.getDAOFactory().getSampleDAO();
        List<TechId> techIds = sampleDAO.listSampleIdsByProjectIds(TechId.createList(projectTechId));
        List<Long> techIdsAsLongs = new ArrayList<>();
        for (TechId id : techIds) {
            Long idId = id.getId();
            techIdsAsLongs.add(idId);
        }
        return sampleDAO.listByIDs(techIdsAsLongs);
    }

    @Transient
    /* Note: modifications of the returned collection will result in an exception. */
    public List<SamplePE> getSamples()
    {
        return new UnmodifiableListDecorator<SamplePE>(getProjectSamples(id));
    }

    public void setSamples(List<SamplePE> samples)
    {
        if (SamplePE.projectSamplesEnabled) {
            List<SamplePE> currentSamples = getProjectSamples(id);
            for (SamplePE samplePE : currentSamples) {
                removeSample(samplePE);
            }
            for (SamplePE sample : samples) {
                addSample(sample);
            }
        }
    }

    @Transient
    public void removeSample(SamplePE sample)
    {
        sample.setProjectInternal(null);
    }

    @Transient
    public void addSample(SamplePE sample)
    {
        sample.setProjectInternal(this);
    }

}
