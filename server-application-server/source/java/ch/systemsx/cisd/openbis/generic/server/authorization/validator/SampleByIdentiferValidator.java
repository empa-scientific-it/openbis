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

import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromSampleIdentifierString;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Validator based on a sample identifier.
 * 
 * @author Franz-Josef Elmer
 */
public class SampleByIdentiferValidator extends AbstractIdentifierValidator
{
    @Override
    protected String extractSpaceCodeOrNull(String identifier)
    {
        SampleOwnerIdentifier sampleIdentifier =
                SampleIdentifierFactory.parse(identifier).createSampleOwnerIdentifier();
        SpaceIdentifier spaceLevel = sampleIdentifier.getSpaceLevel();
        return spaceLevel != null ? spaceLevel.getSpaceCode() : null;
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
            return isValidPA(person, new ProjectProviderFromSampleIdentifierString(value.getIdentifier()));
        }
    }

}
