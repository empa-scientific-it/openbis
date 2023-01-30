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

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Validator based on the experiment or sample identifier of a data set.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataSetByExperimentOrSampleIdentifierValidator<DATA_SET> extends AbstractValidator<DATA_SET>
{
    private final ExperimentByIdentiferValidator experimentValidator = new ExperimentByIdentiferValidator();

    private final SampleByIdentiferValidator sampleValidator = new SampleByIdentiferValidator();

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        experimentValidator.init(provider);
        sampleValidator.init(provider);
    }

    @Override
    public boolean doValidation(PersonPE person, final DATA_SET dataSet)
    {
        if (getExperimentIdentifier(dataSet) != null)
        {
            return experimentValidator.isValid(person, new IIdentifierHolder()
                {
                    @Override
                    public String getIdentifier()
                    {
                        return getExperimentIdentifier(dataSet);
                    }
                });
        }
        return sampleValidator.isValid(person, new IIdentifierHolder()
            {
                @Override
                public String getIdentifier()
                {
                    return getSampleIdentifier(dataSet);
                }
            });
    }

    protected abstract boolean isStorageConfirmed(DATA_SET dataSet);

    protected abstract String getExperimentIdentifier(DATA_SET dataSet);

    protected abstract String getSampleIdentifier(DATA_SET dataSet);

}
