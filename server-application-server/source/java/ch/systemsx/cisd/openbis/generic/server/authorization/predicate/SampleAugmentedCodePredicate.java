/*
 * Copyright 2013 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Predicate based on the augmented code of a sample.
 * 
 * @author Bernd Rinn
 */
public class SampleAugmentedCodePredicate extends DelegatedPredicate<SampleIdentifier, String>
{
    public SampleAugmentedCodePredicate()
    {
        this(true);
    }

    public SampleAugmentedCodePredicate(boolean isReadAccess)
    {
        super(new SampleIdentifierPredicate(isReadAccess));
    }

    @Override
    public SampleIdentifier tryConvert(String value)
    {
        return new SampleIdentifierFactory(value).createIdentifier();
    }

    @Override
    public String getCandidateDescription()
    {
        return "sample";
    }

}
