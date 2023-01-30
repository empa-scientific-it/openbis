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

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link IValidator} implementation suitable for {@link AbstractExternalData}.
 * 
 * @author Tomasz Pylak
 */
public final class ExternalDataValidator extends AbstractValidator<AbstractExternalData>
{
    private final IValidator<Space> spaceValidator;

    private final IValidator<Project> projectValidator;

    private final IValidator<AbstractExternalData> storageConfirmedValidator;

    public ExternalDataValidator()
    {
        spaceValidator = new SpaceValidator();

        projectValidator = new ProjectValidator();

        storageConfirmedValidator = new StorageConfirmedForAdminValidator();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        spaceValidator.init(provider);
        projectValidator.init(provider);
        storageConfirmedValidator.init(provider);
    }

    //
    // IValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final AbstractExternalData dataSet)
    {
        return (isSpaceValid(person, dataSet) || isProjectValid(person, dataSet)) && storageConfirmedValidator.isValid(person, dataSet);
    }

    private boolean isSpaceValid(final PersonPE person, final AbstractExternalData dataSet)
    {
        return spaceValidator.isValid(person, dataSet.getSpace());
    }

    private boolean isProjectValid(final PersonPE person, final AbstractExternalData dataSet)
    {
        return dataSet.getProject() != null && projectValidator.isValid(person, dataSet.getProject());
    }

}
