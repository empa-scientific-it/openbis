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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link IValidator} implementation suitable for {@link Experiment}.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentValidator extends AbstractValidator<Experiment>
{
    private final IValidator<Space> groupValidator;

    public ExperimentValidator()
    {
        groupValidator = new SpaceValidator();
    }

    //
    // IValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final Experiment value)
    {
        final Space space = value.getProject().getSpace();
        boolean result = groupValidator.isValid(person, space);

        if (result)
        {
            return result;
        } else
        {
            return isValidPA(person, new ProjectProviderFromProject(value.getProject()));
        }
    }
}
