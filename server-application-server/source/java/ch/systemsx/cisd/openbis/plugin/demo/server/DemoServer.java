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

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SamplePropertyAccessValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleParentWithDerivedDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.MetaprojectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.plugin.demo.shared.IDemoServer;
import ch.systemsx.cisd.openbis.plugin.demo.shared.ResourceNames;

/**
 * The concrete {@link IDemoServer} implementation.
 * 
 * @author Christian Ribeaud
 */
@Component(ResourceNames.DEMO_PLUGIN_SERVER)
public final class DemoServer extends AbstractServer<IDemoServer> implements IDemoServer
{
    @Resource(name = ResourceNames.DEMO_BUSINESS_OBJECT_FACTORY)
    private IDemoBusinessObjectFactory businessObjectFactory;

    @Resource(name = ComponentNames.MANAGED_PROPERTY_EVALUATOR_FACTORY)
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public DemoServer()
    {
    }

    @Private
    DemoServer(final IOpenBisSessionManager sessionManager, final IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            final IDemoBusinessObjectFactory businessObjectFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, propertiesBatchManager, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin);
        this.businessObjectFactory = businessObjectFactory;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    @Override
    public final IDemoServer createLogger(IInvocationLoggerContext context)
    {
        return new DemoServerLogger(getSessionManager(), context);
    }

    //
    // IDemoServer
    //

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(identifier);
        final SamplePE sample = sampleBO.getSample();
        return getSampleInfo(session, sample);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public SampleParentWithDerived getSampleInfo(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class)
            final TechId sampleId) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        final SamplePE sample = sampleBO.getSample();
        return getSampleInfo(session, sample);
    }

    private SampleParentWithDerived getSampleInfo(final Session session, final SamplePE sample)
    {
        ISampleTypeSlaveServerPlugin plugin =
                getSampleTypeSlaveServerPlugin(sample.getSampleType());
        SampleParentWithDerivedDTO sampleInfo = plugin.getSampleInfo(session, sample);
        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), sample);
        return SampleTranslator.translate(sampleInfo, session.getBaseIndexURL(),
                MetaprojectTranslator.translate(metaprojectPEs), managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void registerSample(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            final NewSample newSample, final Collection<NewAttachment> attachments)
    {
        throw new NotImplementedException();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public int getNumberOfExperiments(String sessionToken)
    {
        return getDAOFactory().getExperimentDAO().listExperiments().size();
    }

}
