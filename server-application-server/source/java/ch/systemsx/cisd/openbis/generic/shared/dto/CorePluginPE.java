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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

/**
 * A <i>Persistence Entity</i> which represents an entry in {@link TableNames#CORE_PLUGINS_TABLE}.
 * 
 * @author Kaloyan Enimanev
 */
@Entity
@Table(name = TableNames.CORE_PLUGINS_TABLE, uniqueConstraints =
{ @UniqueConstraint(columnNames =
{ ColumnNames.NAME_COLUMN, ColumnNames.VERSION_COLUMN }) })
public final class CorePluginPE implements Comparable<CorePluginPE>
{
    transient private Long id;

    private String name;

    private Date registrationDate;

    private int version;

    private String masterDataRegistrationScript;

    @SequenceGenerator(name = SequenceNames.CORE_PLUGIN_SEQUENCE, sequenceName = SequenceNames.CORE_PLUGIN_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.CORE_PLUGIN_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Column(name = ColumnNames.NAME_COLUMN)
    @NotNull(message = ValidationMessages.NAME_NOT_NULL_MESSAGE)
    public final String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public final Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @NotNull
    @Column(name = ColumnNames.VERSION_COLUMN, updatable = false)
    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    @Column(name = ColumnNames.MASTER_DATA_REGISTRATION_SCRIPT)
    public String getMasterDataRegistrationScript()
    {
        return masterDataRegistrationScript;
    }

    public void setMasterDataRegistrationScript(String masterDataRegistrationScript)
    {
        this.masterDataRegistrationScript = masterDataRegistrationScript;
    }

    @Override
    public int compareTo(CorePluginPE other)
    {
        if (version != other.version)
        {
            return version - other.version;
        }
        return name.compareTo(other.name);
    }
}
