/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.authorization;

import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IAuthorizationGroupImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IRoleAssignmentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IUserImmutable;

/**
 * @author Jakub Straszewski
 */
public interface IAuthorizationService
{
    boolean doesUserHaveRole(String user, String role, String spaceOrNull);

    List<ISampleImmutable> filterToVisibleSamples(String user, List<ISampleImmutable> samples);

    List<IDataSetImmutable> filterToVisibleDatasets(String user, List<IDataSetImmutable> datasets);

    List<IExperimentImmutable> filterToVisibleExperiments(String user,
            List<IExperimentImmutable> experiments);

    List<IAuthorizationGroupImmutable> listAuthorizationGroups();

    List<IAuthorizationGroupImmutable> listAuthorizationGroupsForUser(String userId);

    List<IUserImmutable> listUsersForAuthorizationGroup(IAuthorizationGroupImmutable authorizationGroup);

    List<IRoleAssignmentImmutable> listRoleAssignments();
}
