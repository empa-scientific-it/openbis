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

import java.util.Date;

/**
 * Super class of DTOs which hold registration data.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractRegistrationHolder extends Id
{
    private static final long serialVersionUID = 1L;

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

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    //
    // IRegistratorHolder
    //

    /**
     * Returns the registrator or <code>null</code> if not known.
     */
    public final PersonPE getRegistrator()
    {
        return registrator;
    }

    /**
     * Sets the person who has registered the experiment.
     * 
     * @throws AssertionError if <code>registratorID</code> is defined but unequal <code>registrator.getId()</code>.
     */
    public final void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }

    public final Date getRegistrationDate()
    {
        return registrationDate;
    }
}
