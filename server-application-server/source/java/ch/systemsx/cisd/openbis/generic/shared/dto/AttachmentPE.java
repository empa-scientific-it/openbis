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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Contains information about an attachment together with its content.
 * 
 * @author Bernd Rinn
 */
@Entity
@Table(name = TableNames.ATTACHMENTS_TABLE, uniqueConstraints = {
        @UniqueConstraint(columnNames = { ColumnNames.EXPERIMENT_COLUMN, ColumnNames.FILE_NAME_COLUMN,
                ColumnNames.VERSION_COLUMN }),
        @UniqueConstraint(columnNames = { ColumnNames.SAMPLE_COLUMN, ColumnNames.FILE_NAME_COLUMN,
                ColumnNames.VERSION_COLUMN }),
        @UniqueConstraint(columnNames = { ColumnNames.PROJECT_COLUMN, ColumnNames.FILE_NAME_COLUMN,
                ColumnNames.VERSION_COLUMN }) })
public class AttachmentPE extends HibernateAbstractRegistrationHolder implements Serializable,
        Comparable<AttachmentPE>, IIdHolder
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final AttachmentPE[] EMPTY_ARRAY = new AttachmentPE[0];

    private String fileName;

    private int version;

    private AttachmentContentPE attachmentContent;

    transient private Long id;

    /**
     * Parent (e.g. an experiment) to which this attachment belongs.
     */
    private ExperimentPE experimentParent;

    private boolean experimentFrozen;

    private SamplePE sampleParent;

    private boolean sampleFrozen;

    private ProjectPE projectParent;

    private boolean projectFrozen;

    private String description;

    private String title;

    /**
     * Returns the file name of the property or <code>null</code>.
     */
    @Column(name = ColumnNames.FILE_NAME_COLUMN)
    @NotNull(message = ValidationMessages.FILE_NAME_NOT_NULL_MESSAGE)
    @Length(max = 255, message = ValidationMessages.FILE_NAME_LENGTH_MESSAGE)
    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(final String fileName)
    {
        this.fileName = fileName;
    }

    @Column(name = ColumnNames.VERSION_COLUMN)
    @NotNull(message = ValidationMessages.VERSION_NOT_NULL_MESSAGE)
    public int getVersion()
    {
        return version;
    }

    public void setVersion(final int version)
    {
        this.version = version;
    }

    @Override
    @SequenceGenerator(name = SequenceNames.ATTACHMENT_SEQUENCE, sequenceName = SequenceNames.ATTACHMENT_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.ATTACHMENT_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = false)
    @Private
    public ExperimentPE getExperimentParentInternal()
    {
        return experimentParent;
    }

    @Private
    // for Hibernate and bean conversion only
    public void setExperimentParentInternal(final ExperimentPE parent)
    {
        this.experimentParent = parent;
        if (parent != null)
        {
            experimentFrozen = parent.isFrozen();
        }
    }

    @NotNull
    @Column(name = ColumnNames.EXPERIMENT_FROZEN_COLUMN, nullable = false)
    public boolean isExperimentFrozen()
    {
        return experimentParent == null ? experimentFrozen : experimentParent.isFrozen();
    }

    public void setExperimentFrozen(boolean experimentFrozen)
    {
        this.experimentFrozen = experimentFrozen;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.ATTACHMENT_CONTENT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.ATTACHMENT_CONTENT_COLUMN)
    public AttachmentContentPE getAttachmentContent()
    {
        return attachmentContent;
    }

    public void setAttachmentContent(final AttachmentContentPE attachmentContent)
    {
        this.attachmentContent = attachmentContent;
    }

    @Column(name = ColumnNames.TITLE_COLUMN)
    @Length(max = 100, message = ValidationMessages.TITLE_LENGTH_MESSAGE)
    public String getTitle()
    {
        return title;
    }

    public void setTitle(final String title)
    {
        this.title = title;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getFileName(), "name");
        EqualsHashUtils.assertDefined(getVersion(), "version");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof AttachmentPE == false)
        {
            return false;
        }
        final AttachmentPE that = (AttachmentPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getFileName(), that.getFileName());
        builder.append(getVersion(), that.getVersion());
        builder.append(getParent(), that.getParent());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getFileName());
        builder.append(getVersion());
        builder.append(getParent());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("fileName", getFileName());
        builder.append("version", getVersion());
        builder.append("parent", getParent());
        return builder.toString();
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final AttachmentPE o)
    {
        final int byFile = getFileName().compareTo(o.getFileName());
        return byFile == 0 ? Integer.valueOf(getVersion()).compareTo(o.getVersion()) : byFile;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.SAMPLE_COLUMN, updatable = false)
    @Private
    public SamplePE getSampleParentInternal()
    {
        return sampleParent;
    }

    @Private
    // for Hibernate and bean conversion only
    public void setSampleParentInternal(final SamplePE parent)
    {
        this.sampleParent = parent;
        if (parent != null)
        {
            sampleFrozen = parent.isFrozen();
        }
    }

    @NotNull
    @Column(name = ColumnNames.SAMPLE_FROZEN_COLUMN, nullable = false)
    public boolean isSampleFrozen()
    {
        return sampleParent == null ? sampleFrozen : sampleParent.isFrozen();
    }

    public void setSampleFrozen(boolean sampleFrozen)
    {
        this.sampleFrozen = sampleFrozen;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PROJECT_COLUMN, updatable = false)
    @Private
    public ProjectPE getProjectParentInternal()
    {
        return projectParent;
    }

    @Private
    // for Hibernate and bean conversion only
    public void setProjectParentInternal(final ProjectPE parent)
    {
        this.projectParent = parent;
        if (parent != null)
        {
            projectFrozen = parent.isFrozen();
        }
    }

    @NotNull
    @Column(name = ColumnNames.PROJECT_FROZEN_COLUMN, nullable = false)
    public boolean isProjectFrozen()
    {
        return projectParent == null ? projectFrozen : projectParent.isFrozen();
    }

    public void setProjectFrozen(boolean projectFrozen)
    {
        this.projectFrozen = projectFrozen;
    }

    @Transient
    public AttachmentHolderPE getParent()
    {
        if (getExperimentParentInternal() != null)
        {
            return getExperimentParentInternal();
        } else if (getSampleParentInternal() != null)
        {
            return getSampleParentInternal();
        } else if (getProjectParentInternal() != null)
        {
            return getProjectParentInternal();
        }
        return null;
    }

    public void setParent(AttachmentHolderPE owner)
    {
        if (owner instanceof ExperimentPE)
        {
            setExperimentParentInternal((ExperimentPE) owner);
        } else if (owner instanceof SamplePE)
        {
            setSampleParentInternal((SamplePE) owner);
        } else if (owner instanceof ProjectPE)
        {
            setProjectParentInternal((ProjectPE) owner);
        } else
        {
            throw new IllegalStateException("Unexpected attachment holder.");
        }
    }

}
