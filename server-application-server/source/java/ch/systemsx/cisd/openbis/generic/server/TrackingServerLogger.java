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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Logger class for {@link TrackingServer} which creates readable logs of method invocations.
 * 
 * @author Piotr Buczek
 */
final class TrackingServerLogger extends AbstractServerLogger implements ITrackingServer
{
    /**
     * Creates an instance for the specified session manager, invocation status and elapsed time. The session manager is used to retrieve user
     * information which will be a part of the log message.
     */
    TrackingServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    //
    // ITrackingServer
    //

    @Override
    public List<AbstractExternalData> listDataSets(String sessionToken, TrackingDataSetCriteria criteria)
    {
        logTracking(sessionToken, "list_data_sets", "SAMPLE_TYPE(%s) LAST_DATASET_ID(%s)", criteria
                .getConnectedSampleTypeCode(), criteria.getLastSeenDataSetId());
        return null;
    }

    @Override
    public List<Sample> listSamples(String sessionToken, TrackingSampleCriteria criteria)
    {
        logTracking(sessionToken, "list_samples",
                "PROPERTY(%s) VALUE(%s) ALREADY_TRACKED_SIZE(%s)", criteria.getPropertyTypeCode(),
                criteria.getPropertyValue(), criteria.getAlreadyTrackedSampleIds().size());
        return null;
    }

}
