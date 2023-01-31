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
 * Holder object for {@link IDssSessionAuthorizer}.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Bernd Rinn
 */
public class DssSessionAuthorizationHolder
{

    private static IDssSessionAuthorizer authorizer = new IDssSessionAuthorizer()
        {
            @Override
            public Status checkDatasetAccess(String sessionToken,
                    List<String> datasetCodes)
            {
                return Status.createError("Data set authorizer not set.");
            }

            @Override
            public Status checkDatasetAccess(String sessionToken, String datasetCode)
            {
                return Status.createError("Data set authorizer not set.");
            }

            @Override
            public Status checkSpaceWriteable(String sessionToken, SpaceIdentifier spaceId)
            {
                return Status.createError("Data set authorizer not set.");
            }

            @Override
            public Status checkExperimentWriteable(String sessionToken, String experimentIdentifier)
            {
                return Status.createError("Data set authorizer not set.");
            }

            @Override
            public Status checkSampleWriteable(String sessionToken, String sampleIdentifier)
            {
                return Status.createError("Data set authorizer not set.");
            }

            @Override
            public Status checkInstanceAdminAuthorization(String sessionToken)
            {
                return Status.createError("Data set authorizer not set.");
            }

            @Override
            public Status checkProjectPowerUserAuthorization(String sessionToken)
            {
                return Status.createError("Data set authorizer not set.");
            }
        };

    /**
     * Returns the authorizer that can be used to check whether a session is authorized to access a data set code.
     */
    public static IDssSessionAuthorizer getAuthorizer()
    {
        return authorizer;
    }

    /**
     * Sets the authorizer.
     * <p>
     * <i>Only set this method once at program startup.</i>
     */
    public static void setAuthorizer(IDssSessionAuthorizer authorizer)
    {
        DssSessionAuthorizationHolder.authorizer = authorizer;
    }

}
