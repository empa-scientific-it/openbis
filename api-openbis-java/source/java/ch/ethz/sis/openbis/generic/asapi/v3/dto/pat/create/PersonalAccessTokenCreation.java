/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create;

import java.util.Date;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.pat.create.PersonalAccessTokenCreation")
public class PersonalAccessTokenCreation implements ICreation, IObjectCreation
{
    private static final long serialVersionUID = 1L;

    private IPersonId ownerId;

    private String sessionName;

    private Date validFromDate;

    private Date validToDate;

    public PersonalAccessTokenCreation()
    {
    }

    public String getSessionName()
    {
        return sessionName;
    }

    public void setSessionName(final String sessionName)
    {
        this.sessionName = sessionName;
    }

    public Date getValidFromDate()
    {
        return validFromDate;
    }

    public void setValidFromDate(final Date validFromDate)
    {
        this.validFromDate = validFromDate;
    }

    public Date getValidToDate()
    {
        return validToDate;
    }

    public void setValidToDate(final Date validToDate)
    {
        this.validToDate = validToDate;
    }

    public IPersonId getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(final IPersonId ownerId)
    {
        this.ownerId = ownerId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("ownerId", ownerId).append("sessionName", sessionName).toString();
    }

}

