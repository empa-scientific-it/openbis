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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromExperimentTechId;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromProjectTechId;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromRolesWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * An <code>IPredicate</code> abstract implementation based on {@link TechId} and {@link SpaceOwnerKind}
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractTechIdPredicate extends AbstractSpacePredicate<TechId>
{
    protected final SpaceOwnerKind entityKind;

    public AbstractTechIdPredicate(SpaceOwnerKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public static AbstractTechIdPredicate create(SpaceOwnerKind entityKind)
    {
        switch (entityKind)
        {
            case EXPERIMENT:
                return new ExperimentTechIdPredicate();
            case PROJECT:
                return new ProjectTechIdPredicate();
            case SPACE:
                return new SpaceTechIdPredicate();
        }
        return null;
    }

    public static class ExperimentTechIdPredicate extends AbstractTechIdPredicate
    {
        public ExperimentTechIdPredicate()
        {
            super(SpaceOwnerKind.EXPERIMENT);
        }

        @Override
        protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, TechId techId)
        {
            IProjectAuthorization<TechId> pa = new ProjectAuthorizationBuilder<TechId>()
                    .withData(authorizationDataProvider)
                    .withUser(new UserProviderFromPersonPE(person))
                    .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                    .withObjects(new ProjectProviderFromExperimentTechId(techId))
                    .build();

            if (pa.getObjectsWithoutAccess().isEmpty())
            {
                return Status.OK;
            }

            return super.doEvaluation(person, allowedRoles, techId);
        }
    }

    public static class SpaceTechIdPredicate extends AbstractTechIdPredicate
    {
        public SpaceTechIdPredicate()
        {
            super(SpaceOwnerKind.SPACE);
        }
    }

    public static class ProjectTechIdPredicate extends AbstractTechIdPredicate
    {
        public ProjectTechIdPredicate()
        {
            super(SpaceOwnerKind.PROJECT);
        }

        @Override
        protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, TechId techId)
        {
            IProjectAuthorization<TechId> pa = new ProjectAuthorizationBuilder<TechId>()
                    .withData(authorizationDataProvider)
                    .withUser(new UserProviderFromPersonPE(person))
                    .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                    .withObjects(new ProjectProviderFromProjectTechId(techId))
                    .build();

            if (pa.getObjectsWithoutAccess().isEmpty())
            {
                return Status.OK;
            }

            return super.doEvaluation(person, allowedRoles, techId);
        }
    }

    //
    // AbstractDatabaseInstancePredicate
    //

    @Override
    public final String getCandidateDescription()
    {
        return "technical id";
    }

    @Override
    protected Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final TechId techId)
    {
        assert initialized : "Predicate has not been initialized";

        final SpacePE spaceOrNull = authorizationDataProvider.tryGetSpace(entityKind, techId);
        return evaluateSpace(person, allowedRoles, spaceOrNull);
    }

}
