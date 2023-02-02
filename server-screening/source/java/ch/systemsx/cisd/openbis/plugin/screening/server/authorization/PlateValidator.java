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

package ch.systemsx.cisd.openbis.plugin.screening.server.authorization;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.AbstractValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;

/**
 * @author pkupczyk
 */
public class PlateValidator extends AbstractValidator<Plate>
{

    private SimpleSpaceValidator spaceValidator = new SimpleSpaceValidator();

    private ExperimentByIdentiferValidator experimentValidator = new ExperimentByIdentiferValidator();

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        experimentValidator.init(provider);
    }

    @Override
    public boolean doValidation(PersonPE person, Plate value)
    {
        if (value.tryGetSpaceCode() != null)
        {
            boolean result = spaceValidator.doValidation(person, new ICodeHolder()
                {
                    @Override
                    public String getCode()
                    {
                        return value.tryGetSpaceCode();
                    }
                });
            if (result)
            {
                return result;
            }
        }

        if (value.getExperimentIdentifier() != null)
        {
            return experimentValidator.doValidation(person, new IIdentifierHolder()
                {
                    @Override
                    public String getIdentifier()
                    {
                        return value.getExperimentIdentifier().getAugmentedCode();
                    }
                });
        }

        return false;
    }

}
