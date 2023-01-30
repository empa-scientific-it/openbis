/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Persistent entity representing operation execution.
 * 
 * @author pkupczyk
 */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = TableNames.OPERATION_EXECUTIONS_TABLE, uniqueConstraints = { @UniqueConstraint(columnNames = { ColumnNames.CODE_COLUMN }) })
public class OperationExecutionPE implements IIdHolder, IIdentityHolder, Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private static final String SEPARATOR = "\n";

    protected Long id;

    private String code;

    private OperationExecutionState state;

    private PersonPE owner;

    private String description;

    private String notification;

    private OperationExecutionAvailability availability;

    private Long availabilityTime;

    private String summaryOperations;

    private String summaryProgress;

    private String summaryError;

    private String summaryResults;

    private OperationExecutionAvailability summaryAvailability;

    private Long summaryAvailabilityTime;

    private String detailsPath;

    private OperationExecutionAvailability detailsAvailability;

    private Long detailsAvailabilityTime;

    private Date creationDate;

    private Date startDate;

    private Date finishDate;

    @Override
    @SequenceGenerator(name = SequenceNames.OPERATION_EXECUTIONS_SEQUENCE, sequenceName = SequenceNames.OPERATION_EXECUTIONS_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.OPERATION_EXECUTIONS_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    @Transient
    public String getPermId()
    {
        return getCode();
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        return getCode();
    }

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.CODE_COLUMN, updatable = false)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @NotNull(message = ValidationMessages.STATE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.STATE_COLUMN)
    @Enumerated(EnumType.STRING)
    public OperationExecutionState getState()
    {
        return state;
    }

    public void setState(OperationExecutionState state)
    {
        this.state = state;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.OWNER_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.OWNER_COLUMN, updatable = false)
    public PersonPE getOwner()
    {
        return owner;
    }

    public void setOwner(PersonPE owner)
    {
        this.owner = owner;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Column(name = ColumnNames.NOTIFICATION_COLUMN)
    public String getNotification()
    {
        return notification;
    }

    public void setNotification(String notification)
    {
        this.notification = notification;
    }

    @Column(name = ColumnNames.AVAILABILITY_COLUMN)
    @Enumerated(EnumType.STRING)
    public OperationExecutionAvailability getAvailability()
    {
        return availability;
    }

    public void setAvailability(OperationExecutionAvailability availability)
    {
        this.availability = availability;
    }

    @Column(name = ColumnNames.AVAILABILITY_TIME_COLUMN)
    public Long getAvailabilityTime()
    {
        return availabilityTime;
    }

    public void setAvailabilityTime(Long availabilityTime)
    {
        this.availabilityTime = availabilityTime;
    }

    @Column(name = ColumnNames.SUMMARY_OPERATIONS_COLUMN)
    public String getSummaryOperations()
    {
        return summaryOperations;
    }

    public void setSummaryOperations(String summaryOperations)
    {
        this.summaryOperations = summaryOperations;
    }

    @Transient
    public List<String> getSummaryOperationsList()
    {
        return splitString(getSummaryOperations());
    }

    public void setSummaryOperationsList(List<String> summaryOperationsList)
    {
        this.summaryOperations = joinList(summaryOperationsList);
    }

    @Column(name = ColumnNames.SUMMARY_PROGRESS_COLUMN)
    public String getSummaryProgress()
    {
        return summaryProgress;
    }

    public void setSummaryProgress(String summaryProgress)
    {
        this.summaryProgress = summaryProgress;
    }

    @Column(name = ColumnNames.SUMMARY_ERROR_COLUMN)
    public String getSummaryError()
    {
        return summaryError;
    }

    public void setSummaryError(String summaryError)
    {
        this.summaryError = summaryError;
    }

    @Column(name = ColumnNames.SUMMARY_RESULTS_COLUMN)
    public String getSummaryResults()
    {
        return summaryResults;
    }

    public void setSummaryResults(String summaryResults)
    {
        this.summaryResults = summaryResults;
    }

    @Transient
    public List<String> getSummaryResultsList()
    {
        return splitString(getSummaryResults());
    }

    public void setSummaryResultsList(List<String> summaryResultsList)
    {
        this.summaryResults = joinList(summaryResultsList);
    }

    @Column(name = ColumnNames.SUMMARY_AVAILABILITY_COLUMN)
    @Enumerated(EnumType.STRING)
    public OperationExecutionAvailability getSummaryAvailability()
    {
        return summaryAvailability;
    }

    public void setSummaryAvailability(OperationExecutionAvailability summaryAvailability)
    {
        this.summaryAvailability = summaryAvailability;
    }

    @Column(name = ColumnNames.SUMMARY_AVAILABILITY_TIME_COLUMN)
    public Long getSummaryAvailabilityTime()
    {
        return summaryAvailabilityTime;
    }

    public void setSummaryAvailabilityTime(Long summaryAvailabilityTime)
    {
        this.summaryAvailabilityTime = summaryAvailabilityTime;
    }

    @Column(name = ColumnNames.DETAILS_PATH_COLUMN)
    @Length(min = 1, max = 1000, message = ValidationMessages.DETAILS_PATH_LENGTH_MESSAGE)
    public String getDetailsPath()
    {
        return detailsPath;
    }

    public void setDetailsPath(String detailsPath)
    {
        this.detailsPath = detailsPath;
    }

    @Column(name = ColumnNames.DETAILS_AVAILABILITY_COLUMN)
    @Enumerated(EnumType.STRING)
    public OperationExecutionAvailability getDetailsAvailability()
    {
        return detailsAvailability;
    }

    public void setDetailsAvailability(OperationExecutionAvailability detailsAvailability)
    {
        this.detailsAvailability = detailsAvailability;
    }

    @Column(name = ColumnNames.DETAILS_AVAILABILITY_TIME_COLUMN)
    public Long getDetailsAvailabilityTime()
    {
        return detailsAvailabilityTime;
    }

    public void setDetailsAvailabilityTime(Long detailsAvailabilityTime)
    {
        this.detailsAvailabilityTime = detailsAvailabilityTime;
    }

    @Column(name = ColumnNames.CREATION_DATE_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public Date getCreationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(creationDate);
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    @Column(name = ColumnNames.START_DATE_COLUMN)
    public Date getStartDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(startDate);
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    @Column(name = ColumnNames.FINISH_DATE_COLUMN)
    public Date getFinishDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(finishDate);
    }

    public void setFinishDate(Date finishDate)
    {
        this.finishDate = finishDate;
    }

    @Transient
    public Long getAvailabilityTimeLeft()
    {
        return getAvailabilityTimeLeft(getAvailabilityTime());
    }

    @Transient
    public Long getSummaryAvailabilityTimeLeft()
    {
        return getAvailabilityTimeLeft(getSummaryAvailabilityTime());
    }

    @Transient
    public Long getDetailsAvailabilityTimeLeft()
    {
        return getAvailabilityTimeLeft(getDetailsAvailabilityTime());
    }

    private Long getAvailabilityTimeLeft(Long time)
    {
        if (getFinishDate() != null && time != null)
        {
            return getFinishDate().getTime() + time * 1000 - System.currentTimeMillis();
        } else
        {
            return null;
        }
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof OperationExecutionPE == false)
        {
            return false;
        }

        final OperationExecutionPE that = (OperationExecutionPE) obj;
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
        return getCode();
    }

    private List<String> splitString(String string)
    {
        if (string != null)
        {
            String[] array = string.split(SEPARATOR);
            return Arrays.asList(array);
        } else
        {
            return Collections.emptyList();
        }
    }

    private String joinList(List<String> list)
    {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = list.iterator();

        while (iterator.hasNext())
        {
            String item = iterator.next();

            builder.append(item != null ? item.trim() : "");

            if (iterator.hasNext())
            {
                builder.append(SEPARATOR);
            }
        }

        return builder.toString();
    }

}
