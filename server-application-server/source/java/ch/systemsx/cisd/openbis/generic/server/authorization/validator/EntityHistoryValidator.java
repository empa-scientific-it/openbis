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
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public final class EntityHistoryValidator extends AbstractValidator<EntityHistory>
{

    private final IValidator<ICodeHolder> spaceValidator;

    private final IValidator<Project> projectValidator;

    private final IValidator<Experiment> experimentValidator;

    private final IValidator<Sample> sampleValidator;

    private final IValidator<AbstractExternalData> dataSetValidator;

    public EntityHistoryValidator()
    {
        spaceValidator = new SimpleSpaceOrProjectValidator();
        projectValidator = new ProjectValidator();
        experimentValidator = new ExperimentValidator();
        sampleValidator = new SampleValidator();
        dataSetValidator = new ExternalDataValidator();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        spaceValidator.init(provider);
        projectValidator.init(provider);
        experimentValidator.init(provider);
        sampleValidator.init(provider);
        dataSetValidator.init(provider);
    }

    @Override
    public final boolean doValidation(final PersonPE person, final EntityHistory value)
    {
        boolean valid = true;

        if (valid && value.tryGetRelatedSpace() != null)
        {
            valid = valid && spaceValidator.isValid(person, value.tryGetRelatedSpace());
        }

        if (valid && value.tryGetRelatedProject() != null)
        {
            valid = valid && projectValidator.isValid(person, value.tryGetRelatedProject());
        }

        if (valid && value.tryGetRelatedEntity() != null)
        {
            IEntityInformationHolderWithIdentifier entity = value.tryGetRelatedEntity();

            if (entity instanceof Experiment)
            {
                valid = valid && experimentValidator.isValid(person, (Experiment) entity);
            } else if (entity instanceof Sample)
            {
                valid = valid && sampleValidator.isValid(person, (Sample) entity);
            } else if (entity instanceof AbstractExternalData)
            {
                valid = valid && dataSetValidator.isValid(person, (AbstractExternalData) entity);
            } else
            {
                throw new IllegalArgumentException("Unsupporeted related entity: " + entity.getClass());
            }
        }

        return valid;
    }
}
