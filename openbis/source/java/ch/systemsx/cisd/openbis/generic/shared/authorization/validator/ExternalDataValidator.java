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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link IValidator} implementation suitable for {@link ExternalDataPE}.
 * 
 * @author Tomasz Pylak
 */
public final class ExternalDataValidator extends AbstractValidator<ExternalDataPE>
{
    private final IValidator<GroupPE> groupValidator;

    public ExternalDataValidator()
    {
        groupValidator = new GroupValidator();
    }

    //
    // IValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final ExternalDataPE value)
    {
        final GroupPE group = value.getExperiment().getProject().getGroup();
        return groupValidator.isValid(person, group);
    }
}
