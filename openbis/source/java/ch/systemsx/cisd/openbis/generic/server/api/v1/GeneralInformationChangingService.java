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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.GENERAL_INFORMATION_CHANGING_SERVICE_SERVER)
public class GeneralInformationChangingService extends
        AbstractServer<IGeneralInformationChangingService> implements
        IGeneralInformationChangingService
{
    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer genericServer;

    // Default constructor needed by Spring
    public GeneralInformationChangingService()
    {
    }

    GeneralInformationChangingService(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager, IGenericServer genericServer)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.genericServer = genericServer;
    }

    public IGeneralInformationChangingService createLogger(IInvocationLoggerContext context)
    {
        return new GeneralInformationChangingServiceLogger(sessionManager, context);
    }
    
    public void updateSampleProperties(String sessionToken, long sampleID,
            Map<String, String> properties)
    {
        TechId id = new TechId(sampleID);
        Sample sample = genericServer.getSampleInfo(sessionToken, id).getParent();
        for (Entry<String, String> entry : properties.entrySet())
        {
            EntityHelper.createOrUpdateProperty(sample, entry.getKey(), entry.getValue());
        }
        Experiment experiment = sample.getExperiment();
        ExperimentIdentifier experimentIdentifier =
                experiment == null ? null : ExperimentIdentifierFactory.parse(experiment
                        .getIdentifier());
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(sample.getIdentifier());
        Sample container = sample.getContainer();
        String containerIdentifier = container == null ? null : container.getIdentifier();
        SampleUpdatesDTO updates =
                new SampleUpdatesDTO(id, sample.getProperties(), experimentIdentifier,
                        Collections.<NewAttachment> emptySet(), sample.getModificationDate(),
                        sampleIdentifier, containerIdentifier, null);
        genericServer.updateSample(sessionToken, updates);
    }

    public int getMajorVersion()
    {
        return 1;
    }
    
    public int getMinorVersion()
    {
        return 0;
    }

}
