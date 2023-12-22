/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author pkupczyk
 */
public class UpdateEntityPropertyExecutorTest extends AbstractEntityPropertyExecutorTest
{

    private IEntityTypeDAO entityTypeDAO;

    private IEntityPropertyTypeDAO entityPropertyTypeDAO;

    private IPropertyTypeDAO propertyTypeDAO;

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @Override
    protected void init()
    {
        entityTypeDAO = context.mock(IEntityTypeDAO.class);
        entityPropertyTypeDAO = context.mock(IEntityPropertyTypeDAO.class);
        propertyTypeDAO = context.mock(IPropertyTypeDAO.class);
        managedPropertyEvaluatorFactory = context.mock(IManagedPropertyEvaluatorFactory.class);
    }

    @Test
    public void testUpdateProperties()
    {
        final Session session = createSession();

        final EntityTypePE entityType = createEntityType();

        final Set<EntityPropertyPE> beforeProperties = new HashSet<EntityPropertyPE>();
        beforeProperties.add(createEntityProperty("TEST_PROPERTY_1", "value 1"));
        beforeProperties.add(createEntityProperty("TEST_PROPERTY_2", "value 2"));

        final Set<EntityPropertyPE> afterProperties = new HashSet<EntityPropertyPE>();
        afterProperties.add(createEntityProperty("TEST_PROPERTY_1", "value 1.1"));
        afterProperties.add(createEntityProperty("TEST_PROPERTY_2", "value 2"));
        afterProperties.add(createEntityProperty("TEST_PROPERTY_3", "value 3"));

        final IEntityInformationWithPropertiesHolder entityPropertiesHolder = context.mock(IEntityInformationWithPropertiesHolder.class);

        context.checking(new Expectations()
            {
                {
                    allowing(operationContext).pushProgress(with(any(IProgress.class)));
                    allowing(operationContext).popProgress();

                    allowing(operationContext).getSession();
                    will(returnValue(session));

                    allowing(daoFactory).getEntityTypeDAO(EntityKind.SAMPLE);
                    will(returnValue(entityTypeDAO));

                    allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.SAMPLE);
                    will(returnValue(entityPropertyTypeDAO));

                    allowing(daoFactory).getPropertyTypeDAO();
                    will(returnValue(propertyTypeDAO));

                    allowing(entityTypeDAO).listEntityTypes();
                    will(returnValue(Arrays.asList(entityType)));

                    allowing(entityPropertyTypeDAO).listEntityPropertyTypes(entityType);
                    will(returnValue(Arrays.asList(createEntityTypePropertyType("TEST_PROPERTY_1"), createEntityTypePropertyType("TEST_PROPERTY_2"),
                            createEntityTypePropertyType("TEST_PROPERTY_3"))));

                    allowing(propertyTypeDAO).tryFindPropertyTypeByCode("TEST_PROPERTY_1");
                    will(returnValue(createPropertyType("TEST_PROPERTY_1")));

                    allowing(propertyTypeDAO).tryFindPropertyTypeByCode("TEST_PROPERTY_2");
                    will(returnValue(createPropertyType("TEST_PROPERTY_2")));

                    allowing(propertyTypeDAO).tryFindPropertyTypeByCode("TEST_PROPERTY_3");
                    will(returnValue(createPropertyType("TEST_PROPERTY_3")));

                    one(entityPropertiesHolder).getProperties();
                    will(returnValue(beforeProperties));

                    allowing(entityPropertiesHolder).getEntityType();
                    will(returnValue(entityType));

                    one(entityPropertiesHolder).setProperties(with(new PropertyValuesMatcher(afterProperties)));
                }
            });

        Map<String, Serializable> updatedPropertyValues = new HashMap<String, Serializable>();
        updatedPropertyValues.put("TEST_PROPERTY_1", "value 1.1");
        updatedPropertyValues.put("TEST_PROPERTY_3", "value 3");

        execute(entityPropertiesHolder, entityType, updatedPropertyValues);
    }

    private void execute(IEntityInformationWithPropertiesHolder entity, EntityTypePE entityType, final Map<String, Serializable> propertiesMap)
    {
        UpdateEntityPropertyExecutor executor = new UpdateEntityPropertyExecutor(daoFactory, managedPropertyEvaluatorFactory);
        IPropertiesHolder holder = new IPropertiesHolder()
            {
                @Override
                public void setProperty(String propertyName, Serializable propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setProperties(Map<String, Serializable> properties)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Map<String, Serializable> getProperties()
                {
                    return propertiesMap;
                }

                @Override
                public Boolean getBooleanProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setBooleanProperty(String propertyName, Boolean propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getControlledVocabularyProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setControlledVocabularyProperty(String propertyName,
                        String propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<String> getMultiValueControlledVocabularyProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueControlledVocabularyProperty(String propertyName,
                        List<String> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<SamplePermId> getMultiValueSampleProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueSampleProperty(String propertyName,
                        List<SamplePermId> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public SamplePermId getSampleProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setSampleProperty(String propertyName, SamplePermId propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getHyperlinkProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setHyperlinkProperty(String propertyName, String propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getXmlProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setXmlProperty(String propertyName, String propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Long getIntegerProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setIntegerProperty(String propertyName, Long propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getStringProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setStringProperty(String propertyName, String propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Double getRealProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setRealProperty(String propertyName, Double propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public ZonedDateTime getTimestampProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setTimestampProperty(String propertyName, ZonedDateTime propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Long[] getIntegerArrayProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setIntegerArrayProperty(String propertyName, Long[] propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Double[] getRealArrayProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setRealArrayProperty(String propertyName, Double[] propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String[] getStringArrayProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setStringArrayProperty(String propertyName, String[] propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public ZonedDateTime[] getTimestampArrayProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setTimestampArrayProperty(String propertyName,
                        ZonedDateTime[] propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getJsonProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setJsonProperty(String propertyName, String propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<Long> getMultiValueIntegerProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueIntegerProperty(String propertyName,
                        List<Long> propertyValues)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<String> getMultiValueStringProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueStringProperty(String propertyName,
                        List<String> propertyValues)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<Double> getMultiValueRealProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueRealProperty(String propertyName,
                        List<Double> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<ZonedDateTime> getMultiValueTimestampProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueTimestampProperty(String propertyName,
                        List<ZonedDateTime> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<Boolean> getMultiValueBooleanProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueBooleanProperty(String propertyName,
                        List<Boolean> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<String> getMultiValueHyperlinkProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueHyperlinkProperty(String propertyName,
                        List<String> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<String> getMultiValueXmlProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueXmlProperty(String propertyName,
                        List<String> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<Long[]> getMultiValueIntegerArrayProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueIntegerArrayProperty(String propertyName,
                        List<Long[]> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<Double[]> getMultiValueRealArrayProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueRealArrayProperty(String propertyName,
                        List<Double[]> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<String[]> getMultiValueStringArrayProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueStringArrayProperty(String propertyName,
                        List<String[]> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<ZonedDateTime[]> getMultiValueTimestampArrayProperty(
                        String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueTimestampArrayProperty(String propertyName,
                        List<ZonedDateTime[]> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<String> getMultiValueJsonProperty(String propertyName)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultiValueJsonProperty(String propertyName,
                        List<String> propertyValue)
                {
                    throw new UnsupportedOperationException();
                }

            };
        MapBatch<IPropertiesHolder, IEntityInformationWithPropertiesHolder> batch =
                new MapBatch<>(0, 0, 1, Collections.singletonMap(holder, entity), 1);
        executor.update(operationContext, batch);
    }

}
