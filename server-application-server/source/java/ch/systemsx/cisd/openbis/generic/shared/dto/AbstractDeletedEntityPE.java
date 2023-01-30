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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Persistence Entity representing deleted experiment.
 * 
 * @author Piotr Buczek
 */
@MappedSuperclass
abstract class AbstractDeletedEntityPE implements IDeletablePE, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    // needed

    private DeletionPE deletion;

    private Integer originalDeletion;

    // additional info

    private String code;

    @Override
    @Column(name = ColumnNames.CODE_COLUMN)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getCode()
    {
        return code;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    @Override
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

    @Column(name = ColumnNames.ORIGINAL_DELETION_COLUMN, nullable = false)
    public Integer getOriginalDeletion()
    {
        return originalDeletion;
    }

    public void setOriginalDeletion(Integer originalDeletion)
    {
        this.originalDeletion = originalDeletion;
    }

    //
    // Object
    //

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("deletion", getDeletion());
        return builder.toString();
    }

}
