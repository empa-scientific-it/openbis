/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityTypeExecutor;
import ch.systemsx.cisd.openbis.generic.server.DataStoreServiceRegistrator;
import ch.systemsx.cisd.openbis.generic.server.IDataStoreServiceRegistrator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class CreateDataSetTypesExecutor extends AbstractCreateEntityTypeExecutor<DataSetTypeCreation, DataSetType, DataSetTypePE>
        implements ICreateDataSetTypeExecutor, InitializingBean
{
    @Autowired
    private IDataSetTypeAuthorizationExecutor authorizationExecutor;

    private IDataStoreServiceRegistrator dataStoreServiceRegistrator;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        dataStoreServiceRegistrator = new DataStoreServiceRegistrator(daoFactory);
    }

    @Override
    protected ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind getPEEntityKind()
    {
        return ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.DATA_SET;
    }

    @Override
    protected EntityKind getDAOEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    protected DataSetType newType()
    {
        return new DataSetType();
    }

    @Override
    protected void checkTypeSpecificFields(DataSetTypeCreation creation)
    {
    }

    @Override
    protected void fillTypeSpecificFields(DataSetType type, DataSetTypeCreation creation)
    {
        type.setMainDataSetPattern(creation.getMainDataSetPattern());
        type.setMainDataSetPath(creation.getMainDataSetPath());
        type.setDeletionDisallow(creation.isDisallowDeletion());
        type.setMetaData(creation.getMetaData());
    }

    @Override
    protected void defineType(IOperationContext context, DataSetType type)
    {
        IEntityTypeBO typeBO = businessObjectFactory.createEntityTypeBO(context.getSession());
        typeBO.define(type);
        typeBO.save();
        dataStoreServiceRegistrator.register(type);
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canCreate(context);
    }

}
