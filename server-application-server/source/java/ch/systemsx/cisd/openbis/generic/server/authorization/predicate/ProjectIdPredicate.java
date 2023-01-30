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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.IProjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Predicate for @IProjectId instances.
 * 
 * @author Franz-Josef Elmer
 */
public class ProjectIdPredicate extends AbstractProjectPredicate<IProjectId>
{

    @Override
    public String getCandidateDescription()
    {
        return "project id";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            IProjectId projectId)
    {
        assert spacePredicate.initialized : "Predicate has not been initialized";
        assert projectTechIdPredicate.initialized : "Predicate has not been initialized";
        assert projectPermIdPredicate.initialized : "Predicate has not been initialized";
        assert projectAugmentedCodePredicate.initialized : "Predicate has not been initialized";

        if (projectId instanceof ProjectIdentifierId)
        {
            ProjectIdentifierId identifierId = (ProjectIdentifierId) projectId;
            return projectAugmentedCodePredicate.doEvaluation(person, allowedRoles,
                    identifierId.getIdentifier());
        }
        if (projectId instanceof ProjectPermIdId)
        {
            ProjectPermIdId permIdId = (ProjectPermIdId) projectId;
            return projectPermIdPredicate.doEvaluation(person, allowedRoles, new PermId(permIdId.getPermId()));
        }
        if (projectId instanceof ProjectTechIdId)
        {
            ProjectTechIdId techIdId = (ProjectTechIdId) projectId;
            return projectTechIdPredicate.doEvaluation(person, allowedRoles,
                    new TechId(techIdId.getTechId()));
        }
        return Status.createError("Unsupported project id: " + projectId);
    }

}
