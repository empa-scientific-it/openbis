/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.VerifyProgress;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.EntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;

/**
 * @author pkupczyk
 */
@Component
public class VerifyEntityPropertyExecutor implements IVerifyEntityPropertyExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IEntityInformationProvider entityInformationProvider;

    @Autowired
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;
    
    @SuppressWarnings("unused")
    private VerifyEntityPropertyExecutor()
    {
    }

    public VerifyEntityPropertyExecutor(IDAOFactory daoFactory, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.daoFactory = daoFactory;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    @Override
    public void verify(IOperationContext context, CollectionBatch<? extends IEntityInformationWithPropertiesHolder> batch)
    {
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();

        new CollectionBatchProcessor<IEntityInformationWithPropertiesHolder>(context, batch)
            {
                @Override
                public void process(IEntityInformationWithPropertiesHolder entity)
                {
                    EntityPropertiesConverter converter = getEntityPropertiesConverter(entity.getEntityKind());
                    converter.checkMandatoryProperties(entity.getProperties(), entity.getEntityType(), cache);
                }

                @Override
                public IProgress createProgress(IEntityInformationWithPropertiesHolder object, int objectIndex, int totalObjectCount)
                {
                    return new VerifyProgress(object, objectIndex, totalObjectCount);
                }
            };
    }

    private EntityPropertiesConverter getEntityPropertiesConverter(EntityKind entityKindOrNull)
    {
        return new EntityPropertiesConverter(
                entityKindOrNull, daoFactory, entityInformationProvider, managedPropertyEvaluatorFactory);
    }

}
