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

package ch.systemsx.cisd.openbis.plugin.demo.server;

import java.util.Collection;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.demo.shared.IDemoServer;

/**
 * The <i>demo</i> specific {@link AbstractServerLogger} extension.
 * 
 * @author Christian Ribeaud
 */
final class DemoServerLogger extends AbstractServerLogger implements IDemoServer
{
    DemoServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    //
    // IDemoServer
    //

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "get_sample_info", "CODE(%s)", identifier);
        return null;
    }

    @Override
    public final SampleParentWithDerived getSampleInfo(final String sessionToken, final TechId sampleId)
    {
        logAccess(sessionToken, "get_sample_info", "ID(%s)", sampleId);
        return null;
    }

    @Override
    public void registerSample(final String sessionToken, final NewSample newSample,
            final Collection<NewAttachment> attachments)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%s) ATTACHMENTS(%s)",
                newSample.getSampleType(), newSample.getIdentifier(), attachments.size());
    }

    @Override
    public int getNumberOfExperiments(String sessionToken)
    {
        logTracking(sessionToken, "get_number_of_experiments", "");
        return 0;
    }
}
