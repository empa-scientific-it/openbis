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
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.AbstractValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;

/**
 * A validator for a list of {@link PlateWellReferenceWithDatasets} objects.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Bernd Rinn
 */
public class PlateWellReferenceWithDatasetsValidator extends
        AbstractValidator<PlateWellReferenceWithDatasets>
{

    private PlateValidator plateValidator = new PlateValidator();

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        plateValidator.init(provider);
    }

    @Override
    public boolean doValidation(PersonPE person, PlateWellReferenceWithDatasets value)
    {
        return plateValidator.doValidation(person, value.getExperimentPlateIdentifier());
    }

}
