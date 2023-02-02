/*
 * Copyright 2010 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromExperimentIdentifierString;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromExperimentPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.AbstractValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;

/**
 * @author pkupczyk
 */
public class ExperimentReferenceValidator extends AbstractValidator<ExperimentReference>
{

    private SimpleSpaceValidator spaceValidator = new SimpleSpaceValidator();

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        spaceValidator.init(provider);
    }

    @Override
    public boolean doValidation(PersonPE person, ExperimentReference value)
    {
        if (value.getSpaceCode() != null)
        {
            boolean result = spaceValidator.doValidation(person, new ICodeHolder()
                {
                    @Override
                    public String getCode()
                    {
                        return value.getSpaceCode();
                    }
                });

            if (result)
            {
                return result;
            } else if (value.getProjectCode() != null && value.getCode() != null)
            {
                ExperimentIdentifier experimentIdentifier =
                        new ExperimentIdentifier(value.getSpaceCode(), value.getProjectCode(), value.getCode());

                return isValidPA(person, new ProjectProviderFromExperimentIdentifierString(experimentIdentifier.toString()));
            } else
            {
                return false;
            }
        }

        if (value.getPermId() != null)
        {
            ExperimentPE experimentPE = authorizationDataProvider.tryGetExperimentByPermId(value.getPermId());
            return isValid(person, experimentPE);
        }

        if (value.getId() != null)
        {
            ExperimentPE experimentPE = authorizationDataProvider.tryGetExperimentByTechId(new TechId(value.getId()));
            return isValid(person, experimentPE);
        }

        return false;
    }

    private boolean isValid(PersonPE person, ExperimentPE experimentPE)
    {
        if (experimentPE != null)
        {
            if (spaceValidator.doValidation(person, experimentPE.getProject().getSpace()))
            {
                return true;
            }

            if (isValidPA(person, new ProjectProviderFromExperimentPE(experimentPE)))
            {
                return true;
            }
        }

        return false;
    }

}
