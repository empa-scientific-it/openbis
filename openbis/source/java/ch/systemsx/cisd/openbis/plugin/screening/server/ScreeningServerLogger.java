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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;

/**
 * The <i>screening</i> specific {@link AbstractServerLogger} extension.
 * 
 * @author Christian Ribeaud
 */
final class ScreeningServerLogger extends AbstractServerLogger implements IScreeningServer
{
    ScreeningServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful)
    {
        super(sessionManager, invocationSuccessful);
    }

    //
    // IScreeningServer
    //

    public final SampleGenerationDTO getSampleInfo(final String sessionToken, final SampleIdentifier identifier)
    {
        logTracking(sessionToken, "get_sample_info", "CODE(%s)", identifier);
        return null;
    }
}
