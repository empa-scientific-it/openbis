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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Predicate for {@link BasicEntityDescription}.
 * 
 * @author Franz-Josef Elmer
 */
public class BasicEntityDescriptionPredicate extends AbstractPredicate<BasicEntityDescription>
{
    private final ExperimentAugmentedCodePredicate experimentPredicate;

    private final SampleAugmentedCodePredicate samplePredicate;

    private final DataSetCodePredicate dataSetPredicate;

    public BasicEntityDescriptionPredicate()
    {
        experimentPredicate = new ExperimentAugmentedCodePredicate();
        samplePredicate = new SampleAugmentedCodePredicate();
        dataSetPredicate = new DataSetCodePredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        experimentPredicate.init(provider);
        samplePredicate.init(provider);
        dataSetPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "basic entity description";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, BasicEntityDescription value)
    {
        EntityKind entityKind = value.getEntityKind();
        String entityIdentifier = value.getEntityIdentifier();
        switch (entityKind)
        {
            case EXPERIMENT:
                return experimentPredicate.doEvaluation(person, allowedRoles, entityIdentifier);
            case SAMPLE:
                return samplePredicate.doEvaluation(person, allowedRoles, entityIdentifier);
            case DATA_SET:
                return dataSetPredicate.doEvaluation(person, allowedRoles, entityIdentifier);
            default:
                return Status.OK;
        }
    }

}
