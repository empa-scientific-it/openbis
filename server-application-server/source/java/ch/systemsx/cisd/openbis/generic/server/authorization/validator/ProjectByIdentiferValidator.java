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

import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromProjectIIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * Validator based on a project identifier.
 * 
 * @author Franz-Josef Elmer
 */
public class ProjectByIdentiferValidator extends AbstractIdentifierValidator
{
    @Override
    protected String extractSpaceCodeOrNull(String identifier)
    {
        return new ProjectIdentifierFactory(identifier).createIdentifier().getSpaceCode();
    }

    @Override
    public boolean doValidation(PersonPE person, IIdentifierHolder value)
    {
        boolean result = super.doValidation(person, value);

        if (result)
        {
            return result;
        } else
        {
            return isValidPA(person, new ProjectProviderFromProjectIIdentifierHolder(value));
        }
    }

}
