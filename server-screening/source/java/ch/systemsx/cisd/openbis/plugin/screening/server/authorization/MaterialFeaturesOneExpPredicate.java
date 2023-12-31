/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.IPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesOneExpCriteria;

/**
 * @author Tomasz Pylak
 */
public final class MaterialFeaturesOneExpPredicate implements
        IPredicate<MaterialFeaturesOneExpCriteria>
{
    private final IPredicate<TechId> experimentPredicate;

    public MaterialFeaturesOneExpPredicate()
    {
        this.experimentPredicate = new ExperimentTechIdPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        experimentPredicate.init(provider);
    }

    @Override
    public final Status evaluate(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final MaterialFeaturesOneExpCriteria value)
    {
        try
        {
            return experimentPredicate.evaluate(person, allowedRoles, value.getExperimentId());
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMessage(), ex);
        }
    }
}
