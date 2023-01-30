/*
 * Copyright 2014 ETH Zuerich, SIS
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
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchDomainSearchResultWithFullEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Validator of @SearchdoquenceSearchResult.
 * 
 * @author Franz-Josef Elmer
 */
public class SearchDomainSearchResultValidator extends AbstractValidator<SearchDomainSearchResultWithFullEntity>
{
    private final ExternalDataValidator dataSetValidator = new ExternalDataValidator();

    private final SampleValidator sampleValidator = new SampleValidator();

    private final ExperimentValidator experimentValidator = new ExperimentValidator();

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        dataSetValidator.init(provider);
        sampleValidator.init(provider);
        experimentValidator.init(provider);
    }

    @Override
    public boolean doValidation(PersonPE person, SearchDomainSearchResultWithFullEntity value)
    {
        IEntityInformationHolderWithPermId entity = value.getEntity();
        if (entity instanceof Sample)
        {
            return sampleValidator.isValid(person, (Sample) entity);
        }
        if (entity instanceof Experiment)
        {
            return experimentValidator.isValid(person, (Experiment) entity);
        }
        if (entity instanceof AbstractExternalData)
        {
            return dataSetValidator.isValid(person, (AbstractExternalData) entity);
        }
        return false;
    }

}
