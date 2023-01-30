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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author pkupczyk
 */
public class VerifyEntityPropertyExecutorTest extends AbstractEntityPropertyExecutorTest
{

    private IEntityPropertyTypeDAO entityPropertyTypeDAO;

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @Override
    protected void init()
    {
        entityPropertyTypeDAO = context.mock(IEntityPropertyTypeDAO.class);
        managedPropertyEvaluatorFactory = context.mock(IManagedPropertyEvaluatorFactory.class);
    }

    @Test
    public void testVerifyWithMandatoryPropertyNotEmpty()
    {
        final Set<EntityPropertyPE> properties = new HashSet<EntityPropertyPE>();
        properties.add(createEntityProperty("TEST_PROPERTY_1", "value 1"));
        properties.add(createEntityProperty("TEST_PROPERTY_2", "value 2"));

        testVerify(properties);
    }

    @Test(expectedExceptions = { UserFailureException.class }, expectedExceptionsMessageRegExp = "Value of mandatory property.*")
    public void testVerifyWithMandatoryPropertyEmpty()
    {
        final Set<EntityPropertyPE> properties = new HashSet<EntityPropertyPE>();
        properties.add(createEntityProperty("TEST_PROPERTY_1", "value 1"));

        testVerify(properties);
    }

    private void testVerify(final Set<EntityPropertyPE> properties)
    {
        final IEntityInformationWithPropertiesHolder propertyHolder = context.mock(IEntityInformationWithPropertiesHolder.class);

        final EntityTypePE entityType = createEntityType();

        final EntityTypePropertyTypePE propertyType1 = createEntityTypePropertyType("TEST_PROPERTY_1");
        final EntityTypePropertyTypePE propertyType2 = createEntityTypePropertyType("TEST_PROPERTY_2");
        propertyType2.setMandatory(true);

        context.checking(new Expectations()
            {
                {
                    allowing(operationContext).pushProgress(with(any(IProgress.class)));
                    allowing(operationContext).popProgress();

                    allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.SAMPLE);
                    will(returnValue(entityPropertyTypeDAO));

                    allowing(entityPropertyTypeDAO).listEntityPropertyTypes(entityType);
                    will(returnValue(Arrays.asList(propertyType1, propertyType2)));

                    allowing(propertyHolder).getEntityKind();
                    will(returnValue(EntityKind.SAMPLE));

                    allowing(propertyHolder).getEntityType();
                    will(returnValue(entityType));

                    allowing(propertyHolder).getProperties();
                    will(returnValue(properties));
                }
            });

        execute(Arrays.asList(propertyHolder));
    }

    public void execute(Collection<IEntityInformationWithPropertiesHolder> propertyHolders)
    {
        CollectionBatch<? extends IEntityInformationWithPropertiesHolder> batch =
                new CollectionBatch<IEntityInformationWithPropertiesHolder>(0, 0, propertyHolders.size(), propertyHolders,
                        propertyHolders.size());

        VerifyEntityPropertyExecutor executor = new VerifyEntityPropertyExecutor(daoFactory, managedPropertyEvaluatorFactory);
        executor.verify(operationContext, batch);
    }

}
