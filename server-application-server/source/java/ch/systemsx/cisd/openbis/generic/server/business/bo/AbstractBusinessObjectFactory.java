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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.common.multiplexer.IMultiplexer;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IEntityOperationChecker;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.EntityResolverQueryFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IEntityResolverQuery;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * An <i>abstract</i> <i>Business Object</i> factory.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractBusinessObjectFactory implements InitializingBean
{
    @Resource(name = ComponentNames.DAO_FACTORY)
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.DSS_FACTORY)
    private IDataStoreServiceFactory dssFactory;

    protected IRelationshipService relationshipService;

    protected IEntityOperationChecker entityOperationChecker;

    private IServiceConversationClientManagerLocal conversationClient;

    @Resource(name = ComponentNames.MANAGED_PROPERTY_EVALUATOR_FACTORY)
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    protected ExposablePropertyPlaceholderConfigurer configurer;

    private final IEntityResolverQuery entityResolver;

    private IMultiplexer multiplexer;

    protected DataSetTypeWithoutExperimentChecker dataSetTypeWithoutExperimentChecker;

    protected AbstractBusinessObjectFactory()
    {
        this.entityResolver = EntityResolverQueryFactory.create();
    }

    protected AbstractBusinessObjectFactory(final IDAOFactory daoFactory,
            IDataStoreServiceFactory dssFactory, IRelationshipService relationshipService,
            IEntityOperationChecker entityOperationChecker,
            IServiceConversationClientManagerLocal conversationClient,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IMultiplexer multiplexer)
    {
        this();
        this.daoFactory = daoFactory;
        this.dssFactory = dssFactory;
        this.relationshipService = relationshipService;
        this.entityOperationChecker = entityOperationChecker;
        this.conversationClient = conversationClient;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
        this.multiplexer = multiplexer;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        dataSetTypeWithoutExperimentChecker = new DataSetTypeWithoutExperimentChecker(
                configurer == null ? new Properties() : configurer.getResolvedProps());
    }

    protected final IDAOFactory getDaoFactory()
    {
        return daoFactory;
    }

    protected final IDataStoreServiceFactory getDSSFactory()
    {
        return dssFactory;
    }

    protected final IRelationshipService getRelationshipService()
    {
        return this.relationshipService;
    }

    protected IEntityOperationChecker getEntityOperationChecker()
    {
        return entityOperationChecker;
    }

    protected IServiceConversationClientManagerLocal getConversationClient()
    {
        return conversationClient;
    }

    protected IManagedPropertyEvaluatorFactory getManagedPropertyEvaluatorFactory()
    {
        return managedPropertyEvaluatorFactory;
    }

    /**
     * Returns the entity resolver.
     */
    public IEntityResolverQuery getEntityResolver()
    {
        return entityResolver;
    }

    protected IMultiplexer getMultiplexer()
    {
        return multiplexer;
    }

}
