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

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;

/**
 * An abstract <code>IPredicate</code> for experiments.
 * 
 * @author Bernd Rinn
 */
abstract class AbstractExperimentPredicate<T> extends AbstractPredicate<T>
{
    protected final ExperimentTechIdPredicate experimentTechIdPredicate;

    protected final ProjectIdentifierPredicate projectPredicate;

    protected final ExperimentPermIdPredicate experimentPermIdPredicate;

    protected final ExperimentAugmentedCodePredicate experimentAugmentedCodePredicate;

    public AbstractExperimentPredicate()
    {
        this.experimentTechIdPredicate = new ExperimentTechIdPredicate();
        this.projectPredicate = new ProjectIdentifierPredicate();
        this.experimentPermIdPredicate = new ExperimentPermIdPredicate();
        this.experimentAugmentedCodePredicate = new ExperimentAugmentedCodePredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        experimentTechIdPredicate.init(provider);
        projectPredicate.init(provider);
        experimentPermIdPredicate.init(provider);
        experimentAugmentedCodePredicate.init(provider);
    }
}
