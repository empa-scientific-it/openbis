/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.project;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class ProjectAuthorizationValidator implements IProjectAuthorizationValidator
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public Set<Long> validate(PersonPE person, Collection<Long> projectIds)
    {
        ProjectQuery query = QueryTool.getManagedQuery(ProjectQuery.class);
        List<ProjectAuthorizationRecord> records = query.getAuthorizations(new LongOpenHashSet(projectIds));

        ProjectByIdentiferValidator validator = new ProjectByIdentiferValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        Set<Long> result = new HashSet<Long>();

        for (ProjectAuthorizationRecord record : records)
        {
            final ProjectAuthorizationRecord theRecord = record;

            if (validator.doValidation(person, new IIdentifierHolder()
                {
                    @Override
                    public String getIdentifier()
                    {
                        return new ProjectIdentifier(theRecord.spaceCode, theRecord.code).getIdentifier();
                    }
                }))
            {
                result.add(record.id);
            }
        }

        return result;
    }
}
