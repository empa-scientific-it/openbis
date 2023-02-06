/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.person.search.PersonSearchCriteria")
public class PersonSearchCriteria extends AbstractObjectSearchCriteria<IPersonId>
{

    private static final long serialVersionUID = 1L;

    public PersonSearchCriteria()
    {
    }

    public UserIdSearchCriteria withUserId()
    {
        return with(new UserIdSearchCriteria());
    }

    public UserIdsSearchCriteria withUserIds()
    {
        return with(new UserIdsSearchCriteria());
    }

    public FirstNameSearchCriteria withFirstName()
    {
        return with(new FirstNameSearchCriteria());
    }

    public LastNameSearchCriteria withLastName()
    {
        return with(new LastNameSearchCriteria());
    }

    public EmailSearchCriteria withEmail()
    {
        return with(new EmailSearchCriteria());
    }

    public PersonSearchCriteria withOrOperator()
    {
        return (PersonSearchCriteria) withOperator(SearchOperator.OR);
    }

    public PersonSearchCriteria withAndOperator()
    {
        return (PersonSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("PERSON");
        return builder;
    }

}
