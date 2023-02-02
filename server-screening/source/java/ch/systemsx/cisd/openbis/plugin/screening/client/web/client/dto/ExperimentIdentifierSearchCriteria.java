/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.dto;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;

/**
 * Allows to search in one experiment (given by identifier) or in all of them.
 * 
 * @author Tomasz Pylak
 */
// TODO 2011-06-07, Tomasz Pylak: rename to MaterialDetailViewScope
public class ExperimentIdentifierSearchCriteria
{
    public static ExperimentIdentifierSearchCriteria createSearchAll()
    {
        return new ExperimentIdentifierSearchCriteria(null, null, false);
    }

    public static ExperimentIdentifierSearchCriteria createProjectScope(
            BasicProjectIdentifier project)
    {
        return new ExperimentIdentifierSearchCriteria(null, project, false);
    }

    public static ExperimentIdentifierSearchCriteria createExperimentScope(
            String experimentIdentifier, boolean restrictGlobalScopeLinkToProject)
    {
        return new ExperimentIdentifierSearchCriteria(experimentIdentifier, null,
                restrictGlobalScopeLinkToProject);
    }

    // if null, all experiments are taken into account
    private final String experimentIdentifierOrNull;

    private final BasicProjectIdentifier projectOrNull;

    private final boolean restrictGlobalScopeLinkToProject;

    private ExperimentIdentifierSearchCriteria(String experimentIdentifierOrNull,
            BasicProjectIdentifier projectOrNull, boolean restrictGlobalScopeLinkToProject)
    {
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
        this.projectOrNull = projectOrNull;
        this.restrictGlobalScopeLinkToProject = restrictGlobalScopeLinkToProject;
    }

    public String tryGetExperimentIdentifier()
    {
        return experimentIdentifierOrNull;
    }

    /**
     * Valid only if experiment identifier is present ({@link #tryGetExperimentIdentifier} is not null).<br>
     * It determines the behavior of the link from material detail view in a single experiment context to the material detail view in 'global'
     * context. If this parameter is true, the context will be the project to which the current experiment belongs, otherwise the context will be
     * switched to all experiments.
     */
    public boolean getRestrictGlobalSearchLinkToProject()
    {
        return restrictGlobalScopeLinkToProject;
    }

    public BasicProjectIdentifier tryGetProject()
    {
        return projectOrNull;
    }

    public boolean searchAllExperiments()
    {
        return StringUtils.isBlank(experimentIdentifierOrNull) && projectOrNull == null;
    }

}
