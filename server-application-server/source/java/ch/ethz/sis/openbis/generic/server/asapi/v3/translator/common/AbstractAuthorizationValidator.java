/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common;

import org.springframework.beans.factory.annotation.Autowired;

import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author pkupczyk
 */
public class AbstractAuthorizationValidator
{

    @Autowired
    protected IDAOFactory daoFactory;

    protected boolean isValid(PersonPE person, SpaceIdentifier spaceIdentifier, ProjectIdentifier projectIdentifier)
    {
        AuthorizationDataProvider provider = new AuthorizationDataProvider(daoFactory);

        SimpleSpaceValidator spaceValidator = new SimpleSpaceValidator();
        spaceValidator.init(provider);

        ProjectByIdentiferValidator projectValidator = new ProjectByIdentiferValidator();
        projectValidator.init(provider);

        if (spaceIdentifier != null)
        {
            if (spaceValidator.isValid(person, new ICodeHolder()
                {
                    @Override
                    public String getCode()
                    {
                        return spaceIdentifier.getSpaceCode();
                    }
                }))
            {
                return true;
            } else
            {
                if (projectIdentifier != null)
                {
                    return projectValidator.isValid(person, new IIdentifierHolder()
                        {
                            @Override
                            public String getIdentifier()
                            {
                                return projectIdentifier.toString();
                            }
                        });
                } else
                {
                    return false;
                }
            }
        } else
        {
            return true;
        }
    }

}
