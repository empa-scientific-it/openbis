/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class SimpleSpaceOrProjectValidator extends AbstractValidator<ICodeHolder>
{

    private final IValidator<ICodeHolder> spaceValidator;

    private final IValidator<IIdentifierHolder> projectValidator;

    public SimpleSpaceOrProjectValidator()
    {
        spaceValidator = new SimpleSpaceValidator();
        projectValidator = new ProjectByIdentiferValidator();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        spaceValidator.init(provider);
        projectValidator.init(provider);
    }

    @Override
    public boolean doValidation(PersonPE person, ICodeHolder space)
    {
        return canAccessSpace(person, space) || canAccessAnySpaceProject(person, space);
    }

    private boolean canAccessSpace(PersonPE person, ICodeHolder space)
    {
        return spaceValidator.isValid(person, space);
    }

    private boolean canAccessAnySpaceProject(PersonPE person, ICodeHolder space)
    {
        IAuthorizationConfig config = authorizationDataProvider.getAuthorizationConfig();

        if (config.isProjectLevelEnabled() && config.isProjectLevelUser(person.getUserId()))
        {
            SpacePE spacePE = authorizationDataProvider.tryGetSpace(space.getCode());

            if (spacePE != null)
            {
                List<ProjectPE> projects = spacePE.getProjects();

                for (ProjectPE project : projects)
                {
                    if (projectValidator.isValid(person, project))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
