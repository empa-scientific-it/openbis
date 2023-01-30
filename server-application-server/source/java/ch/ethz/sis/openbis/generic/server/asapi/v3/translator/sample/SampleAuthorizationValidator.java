/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample;

import java.util.*;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.AbstractAuthorizationValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;

/**
 * @author pkupczyk
 */
@Component
public class SampleAuthorizationValidator extends AbstractAuthorizationValidator implements ISampleAuthorizationValidator
{

    @Override
    public Set<Long> validate(PersonPE person, Collection<Long> sampleIds)
    {
        AuthorizationDataProvider provider = new AuthorizationDataProvider(daoFactory);
        Set<SampleAccessPE> accessDatas = provider.getSampleCollectionAccessDataByTechIds(TechId.createList(new ArrayList<Long>(sampleIds)), false);
        Set<Long> result = new LinkedHashSet<>();

        for (SampleAccessPE accessData : accessDatas)
        {
            if (isValid(person, accessData.getSpaceIdentifier(), accessData.getProjectIdentifier()))
            {
                result.add(accessData.getSampleId());
            }
        }

        return result;
    }

}
