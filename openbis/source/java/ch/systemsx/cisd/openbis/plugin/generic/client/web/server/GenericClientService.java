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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.TabFileLoader;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.AbstractClientService;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The {@link IGenericClientService} implementation.
 * 
 * @author Franz-Josef Elmer
 */
@Component(value = ResourceNames.GENERIC_PLUGIN_SERVICE)
public final class GenericClientService extends AbstractClientService implements
        IGenericClientService
{

    @Resource(name = ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer genericServer;

    public GenericClientService()
    {
    }

    @Private
    GenericClientService(final IGenericServer genericServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.genericServer = genericServer;
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return genericServer;
    }

    //
    // IGenericClientService
    //

    public final SampleGeneration getSampleInfo(final String sampleIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final SampleIdentifier identifier = SampleIdentifierFactory.parse(sampleIdentifier);
            final SampleGenerationDTO sampleGeneration =
                    genericServer.getSampleInfo(getSessionToken(), identifier);
            return BeanUtils.createBean(SampleGeneration.class, sampleGeneration, DtoConverters
                    .getSampleConverter());
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerSample(final NewSample newSample)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerSample(sessionToken, newSample);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    // TODO 2008-12-09, Christian Ribeaud: Add sample type here. This means we will use the plugin
    // architecture.
    public final String registerSamples(final String sessionKey)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final HttpSession session = getHttpSession();
            final UploadedFilesBean uploadedFiles =
                    (UploadedFilesBean) session.getAttribute(sessionKey);
            TabFileLoader<NewSample> fileLoader =
                    new TabFileLoader<NewSample>(new IParserObjectFactoryFactory<NewSample>()
                        {
                            //
                            // 
                            //

                            public IParserObjectFactory<NewSample> createFactory(
                                    IPropertyMapper propertyMapper) throws ParserException
                            {
                                // TODO Auto-generated method stub
                                return null;
                            }
                        });

            session.removeAttribute(sessionKey);
            return null;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public Experiment getExperimentInfo(final String experimentIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final ExperimentIdentifier identifier =
                    new ExperimentIdentifierFactory(experimentIdentifier).createIdentifier();
            final ExperimentPE experiment =
                    genericServer.getExperimentInfo(getSessionToken(), identifier);
            return ExperimentTranslator.translate(experiment,
                    ExperimentTranslator.LoadableFields.PROPERTIES,
                    ExperimentTranslator.LoadableFields.ATTACHMENTS);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

}
