/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtils;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class CreatePropertyTypeExecutor
        extends AbstractCreateEntityExecutor<PropertyTypeCreation, PropertyTypePE, PropertyTypePermId>
        implements ICreatePropertyTypeExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISetPropertyTypeVocabularyExecutor setPropertyTypeVocabularyExecutor;

    @Autowired
    private ISetPropertyTypeMaterialTypeExecutor setPropertyTypeMaterialTypeExecutor;

    @Autowired
    private ISetPropertyTypeSampleTypeExecutor setPropertyTypeSampleTypeExecutor;

    @Autowired
    private IPropertyTypeAuthorizationExecutor authorizationExecutor;

    @Override
    protected IObjectId getId(PropertyTypePE entity)
    {
        return new PropertyTypePermId(entity.getCode());
    }

    @Override
    protected PropertyTypePermId createPermId(IOperationContext context, PropertyTypePE entity)
    {
        return new PropertyTypePermId(entity.getCode());
    }

    @Override
    protected void checkData(IOperationContext context, PropertyTypeCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }
        if (creation.isManagedInternally() && false == CodeConverter.isInternalNamespace(creation.getCode()))
        {
            throw new UserFailureException(
                    "Code of an internally managed property type has to start with '" + BasicConstant.INTERNAL_NAMESPACE_PREFIX + "' prefix.");
        }
        if (false == creation.isManagedInternally() && CodeConverter.isInternalNamespace(creation.getCode()))
        {
            throw new UserFailureException(
                    "'" + BasicConstant.INTERNAL_NAMESPACE_PREFIX + "' code prefix can be only used for the internally managed property types.");
        }
        if (StringUtils.isEmpty(creation.getLabel()))
        {
            throw new UserFailureException("Label cannot be empty.");
        }
        if (StringUtils.isEmpty(creation.getDescription()))
        {
            throw new UserFailureException("Description cannot be empty.");
        }
        DataType dataType = creation.getDataType();
        if (dataType == null)
        {
            throw new UserFailureException("Data type not specified.");
        }
        if (dataType == DataType.CONTROLLEDVOCABULARY && creation.getVocabularyId() == null)
        {
            throw new UserFailureException("Data type has been specified as "
                    + DataType.CONTROLLEDVOCABULARY + " but vocabulary id is missing.");
        }
        if (creation.getVocabularyId() != null && dataType != DataType.CONTROLLEDVOCABULARY)
        {
            throw new UserFailureException("Vocabulary id has been specified but data type is " + dataType + ".");
        }
        validateMaterialType(creation, dataType);
        validateSampleType(creation, dataType);
        validateSchemaAndDataType(dataType.name(), creation.getSchema());
        validateTransformationAndDataType(dataType.name(), creation.getTransformation());
    }

    private void validateMaterialType(PropertyTypeCreation creation, DataType dataType)
    {
        IEntityTypeId materialTypeId = creation.getMaterialTypeId();
        if (materialTypeId != null)
        {
            if (dataType != DataType.MATERIAL)
            {
                throw new UserFailureException("Material type id has been specified but data type is " + dataType + ".");
            }
            if (materialTypeId instanceof EntityTypePermId)
            {
                EntityTypePermId permId = (EntityTypePermId) materialTypeId;
                if (permId.getEntityKind() != EntityKind.MATERIAL)
                {
                    throw new UserFailureException("Specified entity type id (" + materialTypeId + ") is not a "
                            + EntityKind.MATERIAL + " type.");
                }
            }
        }
    }

    private void validateSampleType(PropertyTypeCreation creation, DataType dataType)
    {
        IEntityTypeId sampleTypeId = creation.getSampleTypeId();
        if (sampleTypeId != null)
        {
            if (dataType != DataType.SAMPLE)
            {
                throw new UserFailureException("Sample type id has been specified but data type is " + dataType + ".");
            }
            if (sampleTypeId instanceof EntityTypePermId)
            {
                EntityTypePermId permId = (EntityTypePermId) sampleTypeId;
                if (permId.getEntityKind() != EntityKind.SAMPLE)
                {
                    throw new UserFailureException("Specified entity type id (" + sampleTypeId + ") is not a "
                            + EntityKind.SAMPLE + " type.");
                }
            }
        }
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
    }

    @Override
    protected void checkAccess(IOperationContext context, PropertyTypePE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected List<PropertyTypePE> createEntities(IOperationContext context, CollectionBatch<PropertyTypeCreation> batch)
    {
        List<DataTypePE> dataTypes = daoFactory.getPropertyTypeDAO().listDataTypes();
        Map<String, DataTypePE> map = dataTypes.stream().collect(
                Collectors.toMap(t -> t.getCode().toString(), Function.identity()));
        List<PropertyTypePE> propertyTypes = new ArrayList<>();
        PersonPE person = context.getSession().tryGetPerson();
        new CollectionBatchProcessor<PropertyTypeCreation>(context, batch)
            {
                @Override
                public void process(PropertyTypeCreation creation)
                {
                    PropertyTypePE propertyType = new PropertyTypePE();
                    propertyType.setCode(creation.getCode());
                    propertyType.setDescription(creation.getDescription());
                    propertyType.setLabel(creation.getLabel());
                    propertyType.setType(map.get(creation.getDataType().toString()));
                    propertyType.setManagedInternally(creation.isManagedInternally());
                    propertyType.setRegistrator(person);
                    propertyType.setSchema(creation.getSchema());
                    propertyType.setTransformation(creation.getTransformation());
                    propertyType.setMetaData(creation.getMetaData());
                    propertyType.setMultiValue(
                            creation.isMultiValue() != null && creation.isMultiValue());
                    propertyTypes.add(propertyType);
                }

                @Override
                public IProgress createProgress(PropertyTypeCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };
        return propertyTypes;
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<PropertyTypeCreation, PropertyTypePE> batch)
    {
        setPropertyTypeVocabularyExecutor.set(context, batch);
        setPropertyTypeMaterialTypeExecutor.set(context, batch);
        setPropertyTypeSampleTypeExecutor.set(context, batch);
    }

    @Override
    protected List<PropertyTypePE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getPropertyTypeDAO().listAllPropertyTypes();
    }

    @Override
    protected void save(IOperationContext context, List<PropertyTypePE> entities, boolean clearCache)
    {
        for (PropertyTypePE propertyType : entities)
        {
            daoFactory.getPropertyTypeDAO().createPropertyType(propertyType);
        }
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<PropertyTypeCreation, PropertyTypePE> batch)
    {
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "property type", null);
    }

    static void validateSchemaAndDataType(String dataType, String schema)
    {
        if (schema != null)
        {
            if (DataType.XML.name().equals(dataType) == false)
            {
                throw new UserFailureException("XML schema is specified but data type is " + dataType + ".");
            }
            XmlUtils.validateXML(schema, "XML Schema", XmlUtils.XML_SCHEMA_XSD_FILE_RESOURCE);
        }
    }

    static void validateTransformationAndDataType(String dataType, String transformation)
    {
        if (transformation != null)
        {
            if (DataType.XML.name().equals(dataType) == false)
            {
                throw new UserFailureException("XSLT transformation is specified but data type is " + dataType + ".");
            }
            XmlUtils.validateXML(transformation, "XSLT", XmlUtils.XSLT_XSD_FILE_RESOURCE);
        }
    }

}
