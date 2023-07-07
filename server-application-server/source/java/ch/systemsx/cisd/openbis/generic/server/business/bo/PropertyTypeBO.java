/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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

import java.util.Collections;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtils;

/**
 * The only productive implementation of {@link IPropertyTypeBO}.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyTypeBO extends VocabularyBO implements IPropertyTypeBO
{
    private PropertyTypePE propertyTypePE;

    public PropertyTypeBO(final IDAOFactory daoFactory, final Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    //
    // AbstractVocabularyBusinessObject
    //

    @Override
    public final void define(final PropertyType propertyType) throws UserFailureException
    {
        assert propertyType != null : "Unspecified property type.";
        propertyTypePE = new PropertyTypePE();
        propertyTypePE.setCode(propertyType.getCode());
        propertyTypePE.setLabel(propertyType.getLabel());
        propertyTypePE.setDescription(propertyType.getDescription());
        DataTypePE dataTypePE = getDataTypeCode(propertyType.getDataType());
        propertyTypePE.setType(dataTypePE);
        MaterialTypePE materialType = tryGetMaterialType(propertyType.getMaterialType());
        propertyTypePE.setMaterialType(materialType);
        propertyTypePE.setSampleType(tryGetSampleType(propertyType.getSampleType()));
        propertyTypePE.setRegistrator(findPerson());
        propertyTypePE.setManagedInternally(propertyType.isManagedInternally());
        propertyTypePE.setMultiValue(propertyType.isMultiValue());

        if (DataTypeCode.CONTROLLEDVOCABULARY.equals(dataTypePE.getCode()))
        {
            Vocabulary vocabulary = propertyType.getVocabulary();
            if (vocabulary.getId() == null)
            {
                if (false == StringUtils.isBlank(vocabulary.getCode()))
                {
                    String vocabularyCode =
                            CodeConverter.tryToBusinessLayer(vocabulary.getCode(),
                                    vocabulary.isManagedInternally());
                    tryLoad(vocabularyCode);
                }
            } else
            {
                // loading existing vocabulary
                loadVocabularyDataByTechId(TechId.create(vocabulary));
            }

            VocabularyPE vocabularyPE = tryGetVocabulary();
            if (vocabularyPE == null)
            {
                throw new UserFailureException("Vocabulary not selected");
            }

            propertyTypePE.setVocabulary(vocabularyPE);
        }
        // XML data type specific
        propertyTypePE.setSchema(propertyType.getSchema());
        propertyTypePE.setTransformation(propertyType.getTransformation());
        validateXmlDocumentValues();

        new InternalPropertyTypeAuthorization().canCreatePropertyType(session, propertyTypePE);
    }

    private void validateXmlDocumentValues()
    {
        XmlUtils.validateXML(propertyTypePE.getSchema(), "XML Schema", XmlUtils.XML_SCHEMA_XSD_FILE_RESOURCE);
        XmlUtils.validateXML(propertyTypePE.getTransformation(), "XSLT", XmlUtils.XSLT_XSD_FILE_RESOURCE);
    }

    private MaterialTypePE tryGetMaterialType(MaterialType materialType)
    {
        if (materialType != null)
        {
            EntityTypePE entityType =
                    getEntityTypeDAO(EntityKind.MATERIAL).tryToFindEntityTypeByCode(
                            materialType.getCode());
            return (MaterialTypePE) entityType;
        } else
        {
            return null;
        }
    }

    private SampleTypePE tryGetSampleType(SampleType sampleType)
    {
        if (sampleType != null)
        {
            EntityTypePE entityType =
                    getEntityTypeDAO(EntityKind.SAMPLE).tryToFindEntityTypeByCode(
                            sampleType.getCode());
            return (SampleTypePE) entityType;
        } else
        {
            return null;
        }
    }

    private DataTypePE getDataTypeCode(final DataType dataType)
    {
        DataTypePE dataTypePE = null;
        try
        {
            dataTypePE = getPropertyTypeDAO().getDataTypeByCode(dataType.getCode());
        } catch (final IllegalArgumentException e)
        {
            throw UserFailureException.fromTemplate("Unknow data type code '%s'.", dataType);
        }
        assert dataTypePE != null : "Can not be null reaching this point.";
        return dataTypePE;
    }

    @Override
    public final void save() throws UserFailureException
    {
        assert propertyTypePE != null : "Property type not defined.";
        try
        {
            getPropertyTypeDAO().createPropertyType(propertyTypePE);
        } catch (final DataAccessException e)
        {
            throwException(e, String.format("Property type '%s'.", propertyTypePE.getCode()));
        }
    }

    @Override
    public void update(IPropertyTypeUpdates updates)
    {
        loadDataByTechId(TechId.create(updates));
        if (propertyTypePE.getModificationDate().equals(updates.getModificationDate()) == false)
        {
            throwModifiedEntityException("Property type");
        }

        new InternalPropertyTypeAuthorization().canUpdatePropertyType(session, propertyTypePE);

        propertyTypePE.setDescription(updates.getDescription());
        propertyTypePE.setLabel(updates.getLabel());
        propertyTypePE.setSchema(updates.getSchema());
        propertyTypePE.setTransformation(updates.getTransformation());

        validateAndSave();
    }

    private void validateAndSave()
    {
        validateXmlDocumentValues();
        getPropertyTypeDAO().validateAndSaveUpdatedEntity(propertyTypePE);
    }

    @Override
    public final PropertyTypePE getPropertyType()
    {
        assert propertyTypePE != null : "Property type not defined.";
        return propertyTypePE;
    }

    @Override
    public void loadDataByTechId(TechId propertyTypeId)
    {
        try
        {
            propertyTypePE = getPropertyTypeDAO().getByTechId(propertyTypeId);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(exception.getMessage());
        }
    }

    private void loadVocabularyDataByTechId(TechId vocabularyId)
    {
        super.loadDataByTechId(vocabularyId);
    }

    @Override
    public void deleteByTechId(TechId propertyTypeId, String reason) throws UserFailureException
    {
        loadDataByTechId(propertyTypeId);

        new InternalPropertyTypeAuthorization().canDeletePropertyType(session, propertyTypePE);

        try
        {
            getPropertyTypeDAO().delete(propertyTypePE);
            getEventDAO().persist(
                    createDeletionEvent(propertyTypePE, session.tryGetPerson(), reason));
        } catch (final DataIntegrityViolationException ex)
        {
            throwEntityInUseException(
                    String.format("Property Type '%s'", propertyTypePE.getCode()), null);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Property Type '%s'", propertyTypePE.getCode()));
        }
    }

    public static EventPE createDeletionEvent(PropertyTypePE propertyTypePE, PersonPE registrator,
            String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.PROPERTY_TYPE);
        event.setIdentifiers(Collections.singletonList(propertyTypePE.getCode()));
        event.setDescription(getDeletionDescription(propertyTypePE));
        event.setReason(reason);
        event.setRegistrator(registrator);

        return event;
    }

    private static String getDeletionDescription(PropertyTypePE propertyTypePE)
    {
        return String.format("%s", propertyTypePE.getCode());
    }
}
