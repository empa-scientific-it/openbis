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

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Super class of <i>Persistent Entities</i> which hold registration data.
 * <p>
 * <b>Note:</b> there is no <i>NOT-NULL</i> constraint applied to registrator (by comparison with the database where there almost one).
 * </p>
 *
 * @author Christian Ribeaud
 */
@MappedSuperclass
public abstract class HibernateAbstractRegistrationHolder implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    /**
     * Person who registered this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private PersonPE registrator;

    /**
     * Registration date of this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private Date registrationDate;

    /**
     * Ensures that given <var>date</var> is a real one (<code>java.util.Date</code>) and not a <i>SQL</i> one.
     */
    public final static Date getDate(final Date date)
    {
        if (date == null)
        {
            return null;
        }
        final String packageName = date.getClass().getPackage().getName();
        if (packageName.equals("java.sql"))
        {
            return new Date(date.getTime());
        }
        return date;
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public Date getRegistrationDate()
    {
        return getDate(registrationDate);
    }

    @Transient
    public Date getRegistrationDateInternal()
    {
        return registrationDate;
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PERSON_REGISTERER_COLUMN, updatable = false)
    public PersonPE getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }
}
