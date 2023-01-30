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
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ProjectTechIdPredicate;

/**
 * An <code>IPredicate</code> implementation that has the tools for authenticating projects based on any kind of identifier.
 * 
 * @author Bernd Rinn
 */
abstract class AbstractProjectPredicate<T> extends AbstractPredicate<T>
{

    protected IAuthorizationDataProvider provider;

    protected final SpaceIdentifierPredicate spacePredicate;

    protected final ProjectTechIdPredicate projectTechIdPredicate;

    protected final ProjectPermIdPredicate projectPermIdPredicate;

    protected final ProjectAugmentedCodePredicate projectAugmentedCodePredicate;

    public AbstractProjectPredicate()
    {
        this.spacePredicate = new SpaceIdentifierPredicate();
        this.projectTechIdPredicate = new ProjectTechIdPredicate();
        this.projectPermIdPredicate = new ProjectPermIdPredicate();
        this.projectAugmentedCodePredicate = new ProjectAugmentedCodePredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        this.provider = provider;
        spacePredicate.init(provider);
        projectTechIdPredicate.init(provider);
        projectPermIdPredicate.init(provider);
        projectAugmentedCodePredicate.init(provider);
    }

}
