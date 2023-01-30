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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;

/**
 * Abstract Persistence Entity representing a type.
 * 
 * @author Izabela Adamczyk
 */
@MappedSuperclass
public abstract class AbstractTypePE extends AbstractIdAndCodeHolder<AbstractTypePE> implements IIdentityHolder
{
    private static final long serialVersionUID = IServer.VERSION;

    transient protected Long id;

    private String code;

    private String description;

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
        return code;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @org.hibernate.validator.constraints.Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    //
    // AbstractIdAndCodeHolder
    //

    @Override
    @Column(name = ColumnNames.CODE_COLUMN)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getCode()
    {
        return code;
    }

    @Override
    ToStringBuilder createStringBuilder()
    {
        final ToStringBuilder builder = super.createStringBuilder();
        builder.append("description", getDescription());
        return builder;
    }
}
