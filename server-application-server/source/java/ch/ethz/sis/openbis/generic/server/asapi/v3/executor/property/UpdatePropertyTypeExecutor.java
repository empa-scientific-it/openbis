/*
 * Copyright 2018 ETH Zuerich, SIS
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

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.PropertyTypeUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdatePropertyTypeExecutor
        extends AbstractUpdateEntityExecutor<PropertyTypeUpdate, PropertyTypePE, IPropertyTypeId, PropertyTypePermId>
        implements IUpdatePropertyTypeExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapPropertyTypeByIdExecutor mapPropertyTypeByIdExecutor;

    @Autowired
    private IPropertyTypeAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IPropertiesConverter propertiesConverter;

    @Override
    protected IPropertyTypeId getId(PropertyTypeUpdate update)
    {
        return update.getTypeId();
    }

    @Override
    protected PropertyTypePermId getPermId(PropertyTypePE entity)
    {
        return new PropertyTypePermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, PropertyTypeUpdate update)
    {
        if (update.getTypeId() == null)
        {
            throw new UserFailureException("Property type id cannot be null.");
        }
        if (update.getLabel().isModified() && StringUtils.isEmpty(update.getLabel().getValue()))
        {
            throw new UserFailureException("Label cannot be empty.");
        }
        if (update.getDescription().isModified() && StringUtils.isEmpty(update.getDescription().getValue()))
        {
            throw new UserFailureException("Description cannot be empty.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IPropertyTypeId id, PropertyTypePE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<PropertyTypeUpdate, PropertyTypePE> batch)
    {
        new MapBatchProcessor<PropertyTypeUpdate, PropertyTypePE>(context, batch)
            {
                @Override
                public void process(PropertyTypeUpdate update, PropertyTypePE propertyType)
                {
                    propertyType.setDescription(getNewValue(update.getDescription(), propertyType.getDescription()));
                    propertyType.setLabel(getNewValue(update.getLabel(), propertyType.getLabel()));
                    String dataType = propertyType.getType().getCode().name();
                    DataType dataTypeToBeConverted = update.getDataTypeToBeConverted();
                    if (dataTypeToBeConverted != null && dataTypeToBeConverted.name().equals(dataType) == false)
                    {
                        DataType currentDataType = DataType.valueOf(dataType);
                        assertConversionAllowed(currentDataType, dataTypeToBeConverted);
                        DataTypePE newDataType = daoFactory.getPropertyTypeDAO()
                                .getDataTypeByCode(DataTypeCode.valueOf(dataTypeToBeConverted.name()));
                        propertyType.setType(newDataType);
                        if (currentDataType == DataType.CONTROLLEDVOCABULARY &&
                                dataTypeToBeConverted != DataType.CONTROLLEDVOCABULARY) {
                            propertyType.setVocabulary(null);
                        }
                        propertiesConverter.convertProperties(context, propertyType.getCode(), currentDataType, dataTypeToBeConverted);
                    }
                    CreatePropertyTypeExecutor.validateSchemaAndDataType(dataType, update.getSchema().getValue());
                    propertyType.setSchema(getNewValue(update.getSchema(), propertyType.getSchema()));
                    CreatePropertyTypeExecutor.validateTransformationAndDataType(dataType, update.getTransformation().getValue());
                    propertyType.setTransformation(getNewValue(update.getTransformation(), propertyType.getTransformation()));
                    updateMetaData(propertyType, update);
                }

                private void assertConversionAllowed(DataType currentDataType, DataType newDataType)
                {
                    if (EnumSet.of(DataType.VARCHAR, DataType.MULTILINE_VARCHAR).contains(newDataType))
                    {
                        return;
                    }
                    assertConversionAllowed(currentDataType, newDataType, DataType.TIMESTAMP, DataType.DATE);
                    assertConversionAllowed(currentDataType, newDataType, DataType.INTEGER, DataType.REAL);
                }

                private void assertConversionAllowed(DataType currentDataType, DataType newDataType,
                        DataType matchingCurrentDataType, DataType matchingNewDataType)
                {
                    if (newDataType == matchingNewDataType && currentDataType != matchingCurrentDataType)
                    {
                        throw new UserFailureException("Only properties of type " + matchingCurrentDataType
                                + " can be converted to " + newDataType + ", but not " + currentDataType + ".");
                    }
                }

                @SuppressWarnings("unchecked")
                private void updateMetaData(PropertyTypePE propertyType, PropertyTypeUpdate update)
                {
                    Map<String, String> metaData = new HashMap<>();
                    if (propertyType.getMetaData() != null)
                    {
                        metaData.putAll(propertyType.getMetaData());
                    }
                    ListUpdateActionSet<?> lastSetAction = null;
                    AtomicBoolean metaDataChanged = new AtomicBoolean(false);
                    for (ListUpdateAction<Object> action : update.getMetaData().getActions())
                    {
                        if (action instanceof ListUpdateActionAdd<?>)
                        {
                            addTo(metaData, action, metaDataChanged);
                        } else if (action instanceof ListUpdateActionRemove<?>)
                        {
                            for (String key : (Collection<String>) action.getItems())
                            {
                                metaDataChanged.set(true);
                                metaData.remove(key);
                            }
                        } else if (action instanceof ListUpdateActionSet<?>)
                        {
                            lastSetAction = (ListUpdateActionSet<?>) action;
                        }
                    }
                    if (lastSetAction != null)
                    {
                        metaData.clear();
                        addTo(metaData, lastSetAction, metaDataChanged);

                    }
                    if (metaDataChanged.get())
                    {
                        propertyType.setMetaData(metaData.isEmpty() ? null : metaData);
                    }
                }

                @SuppressWarnings("unchecked")
                private void addTo(Map<String, String> metaData, ListUpdateAction<?> lastSetAction, AtomicBoolean metaDataChanged)
                {
                    Collection<Map<String, String>> maps = (Collection<Map<String, String>>) lastSetAction.getItems();
                    for (Map<String, String> map : maps)
                    {
                        if (map.isEmpty() == false)
                        {
                            metaDataChanged.set(true);
                            metaData.putAll(map);
                        }
                    }
                }

                @Override
                public IProgress createProgress(PropertyTypeUpdate key, PropertyTypePE value, int objectIndex, int totalObjectCount)
                {
                    return new UpdateRelationProgress(key, value, "property type", objectIndex, totalObjectCount);
                }
            };
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<PropertyTypeUpdate, PropertyTypePE> batch)
    {
    }

    @Override
    protected Map<IPropertyTypeId, PropertyTypePE> map(IOperationContext context, Collection<IPropertyTypeId> ids)
    {
        return mapPropertyTypeByIdExecutor.map(context, ids);
    }

    @Override
    protected List<PropertyTypePE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getPropertyTypeDAO().listAllEntities();
    }

    @Override
    protected void save(IOperationContext context, List<PropertyTypePE> entities, boolean clearCache)
    {
        for (PropertyTypePE propertyType : entities)
        {
            daoFactory.getPropertyTypeDAO().validateAndSaveUpdatedEntity(propertyType);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "property types", null);
    }

}
