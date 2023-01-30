/*
 * Copyright 2020 ETH Zuerich, SIS
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.Progress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.Batch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.property.IPropertyQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class PropertiesConverter implements IPropertiesConverter
{

    @Override
    public void convertProperties(IOperationContext context, String propertyTypeCode, DataType currentDataType, DataType newDataType)
    {
        IPropertyQuery query = QueryTool.getManagedQuery(IPropertyQuery.class);
        List<IPropertiesListerAndUpdater> listerUpdaters = Arrays.asList(new ExperimentPropertyUpdater(),
                new SamplePropertyUpdater(), new DataSetPropertyUpdater());
        if (currentDataType == DataType.TIMESTAMP && newDataType == DataType.DATE)
        {
            convert(context, query, listerUpdaters, new TimestampToDatePropertyConverter(), propertyTypeCode);
        } else if (currentDataType == DataType.CONTROLLEDVOCABULARY
                && EnumSet.of(DataType.VARCHAR, DataType.MULTILINE_VARCHAR).contains(newDataType))
        {
            convert(context, query, listerUpdaters, new VocabularyToVarcharPropertyConverter(), propertyTypeCode);
        } else if (currentDataType == DataType.SAMPLE
                && EnumSet.of(DataType.VARCHAR, DataType.MULTILINE_VARCHAR).contains(newDataType))
        {
            convert(context, query, listerUpdaters, new SampleToVarcharPropertyConverter(), propertyTypeCode);
        }
    }

    private void convert(IOperationContext context, IPropertyQuery query,
            List<IPropertiesListerAndUpdater> listerUpdaters,
            AbstractPropertyConverter converter, String propertyTypeCode)
    {
        for (IPropertiesListerAndUpdater listerUpdater : listerUpdaters)
        {
            List<PropertyRecord> properties = listerUpdater.listProperties(query, propertyTypeCode);
            for (CollectionBatch<PropertyRecord> batch : Batch.createBatches(properties))
            {
                List<PropertyRecord> convertedProperties = new ArrayList<>();
                new CollectionBatchProcessor<PropertyRecord>(context, batch)
                    {
                        @Override
                        public void process(PropertyRecord property)
                        {
                            convertedProperties.add(converter.convertProperty(property));
                        }

                        @Override
                        public IProgress createProgress(PropertyRecord property, int objectIndex, int totalObjectCount)
                        {
                            return new Progress(String.valueOf(property.objectId), objectIndex, totalObjectCount);
                        }
                    };
                listerUpdater.updateProperties(query, convertedProperties);
            }
        }
    }

    private static interface IPropertiesListerAndUpdater
    {
        List<PropertyRecord> listProperties(IPropertyQuery query, String propertyTypeCode);

        void updateProperties(IPropertyQuery query, List<PropertyRecord> convertedProperties);
    }

    private static class ExperimentPropertyUpdater implements IPropertiesListerAndUpdater
    {
        @Override
        public List<PropertyRecord> listProperties(IPropertyQuery query, String propertyTypeCode)
        {
            return query.listExperimentProperties(propertyTypeCode);
        }

        @Override
        public void updateProperties(IPropertyQuery query, List<PropertyRecord> convertedProperties)
        {
            query.updateExperimentProperties(convertedProperties);
        }
    }

    private static class SamplePropertyUpdater implements IPropertiesListerAndUpdater
    {
        @Override
        public List<PropertyRecord> listProperties(IPropertyQuery query, String propertyTypeCode)
        {
            return query.listSampleProperties(propertyTypeCode);
        }

        @Override
        public void updateProperties(IPropertyQuery query, List<PropertyRecord> convertedProperties)
        {
            query.updateSampleProperties(convertedProperties);
        }
    }

    private static class DataSetPropertyUpdater implements IPropertiesListerAndUpdater
    {
        @Override
        public List<PropertyRecord> listProperties(IPropertyQuery query, String propertyTypeCode)
        {
            return query.listDataSetProperties(propertyTypeCode);
        }

        @Override
        public void updateProperties(IPropertyQuery query, List<PropertyRecord> convertedProperties)
        {
            query.updateDataSetProperties(convertedProperties);
        }
    }

    private static abstract class AbstractPropertyConverter
    {
        PropertyRecord convertProperty(PropertyRecord property)
        {
            PropertyRecord convertedProperty = new PropertyRecord();
            convertedProperty.objectId = property.objectId;
            convert(convertedProperty, property);
            return convertedProperty;
        }

        abstract void convert(PropertyRecord convertedProperty, PropertyRecord property);
    }

    private static class TimestampToDatePropertyConverter extends AbstractPropertyConverter
    {
        @Override
        void convert(PropertyRecord convertedProperty, PropertyRecord property)
        {
            convertedProperty.propertyValue = property.propertyValue.split(" ")[0];
        }
    }

    private static class VocabularyToVarcharPropertyConverter extends AbstractPropertyConverter
    {
        @Override
        void convert(PropertyRecord convertedProperty, PropertyRecord property)
        {
            convertedProperty.propertyValue = property.vocabularyPropertyValue;
        }
    }
    
    private static class SampleToVarcharPropertyConverter extends AbstractPropertyConverter
    {
        @Override
        void convert(PropertyRecord convertedProperty, PropertyRecord property)
        {
            convertedProperty.propertyValue = property.sample_perm_id;
        }
    }
}
