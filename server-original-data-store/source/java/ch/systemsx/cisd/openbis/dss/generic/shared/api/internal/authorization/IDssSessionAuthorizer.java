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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Role that allows to authorize calls in DSS for a given session.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Bernd Rinn
 */
public interface IDssSessionAuthorizer
{
    /**
     * Checks whether the session identified by <var>sessionToken</var> is authorized to access the given <var>datasetCode</var>.
     * 
     * @return {@link Status#OK} if the access is granted, an error status otherwise.
     */
    public Status checkDatasetAccess(String sessionToken, String datasetCode);

    /**
     * Checks whether the session identified by <var>sessionToken</var> is authorized to access the given <var>datasetCodes</var>.
     * 
     * @return {@link Status#OK} if the access is granted, an error status otherwise.
     */
    public Status checkDatasetAccess(String sessionToken, List<String> datasetCodes);

    /**
     * Checks whether the session identified by <var>sessionToken</var> is authorized to write to the given <var>spaceId</var>.
     * 
     * @return {@link Status#OK} if the access is granted, an error status otherwise.
     */
    public Status checkSpaceWriteable(String sessionToken, SpaceIdentifier spaceId);

    /**
     * Checks whether the session identified by <var>sessionToken</var> is authorized to write to the given <var>experimentIdentifier</var>.
     * 
     * @return {@link Status#OK} if the access is granted, an error status otherwise.
     */
    public Status checkExperimentWriteable(String sessionToken, String experimentIdentifier);

    /**
     * Checks whether the session identified by <var>sessionToken</var> is authorized to write to the given <var>sampleIdentifier</var>.
     * 
     * @return {@link Status#OK} if the access is granted, an error status otherwise.
     */
    public Status checkSampleWriteable(String sessionToken, String sampleIdentifier);

    /**
     * Checks whether the session identified by <var>sessionToken</var> has openBIS instance admin privileges.
     * 
     * @return {@link Status#OK} if the access is granted, an error status otherwise.
     */
    public Status checkInstanceAdminAuthorization(String sessionToken);

    /**
     * Checks whether the session identified by <var>sessionToken</var> has openBIS project power user privileges.
     * 
     * @return {@link Status#OK} if the access is granted, an error status otherwise.
     */
    public Status checkProjectPowerUserAuthorization(String sessionToken);
}
