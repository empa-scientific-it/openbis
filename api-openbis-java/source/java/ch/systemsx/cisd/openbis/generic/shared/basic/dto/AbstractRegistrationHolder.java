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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.IRegistratorHolder;

/**
 * Super class of <i>DTO</i>s which hold registration data.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractRegistrationHolder implements Serializable, IRegistratorHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Date registrationDate;

    private Person registrator;

    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @Override
    public final Person getRegistrator()
    {
        return registrator;
    }

    public final void setRegistrator(final Person registrator)
    {
        this.registrator = registrator;
    }

}
