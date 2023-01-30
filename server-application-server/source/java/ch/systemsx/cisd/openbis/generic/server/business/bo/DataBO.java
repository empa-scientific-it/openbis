/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetRegistrationCache;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.DataSetTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ContentCopyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocationType;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewLinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataPE.class)
public class DataBO extends AbstractDataSetBusinessObject implements IDataBO
{
    private DataPE data;

    private DataStorePE dataStore;

    public DataBO(IDAOFactory daoFactory, Session session,
            IRelationshipService relationshipService,
            IServiceConversationClientManagerLocal conversationClient,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker)
    {
        super(daoFactory, session, relationshipService, conversationClient, entityInformationProvider,
                managedPropertyEvaluatorFactory, dataSetTypeChecker);
    }

    public DataBO(IDAOFactory daoFactory, Session exampleSession,
            IEntityPropertiesConverter propertiesConverter,
            IRelationshipService relationshipService,
            IServiceConversationClientManagerLocal conversationClient,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker)
    {
        super(daoFactory, exampleSession, propertiesConverter, relationshipService,
                conversationClient, managedPropertyEvaluatorFactory, dataSetTypeChecker);
    }

    @Override
    public DataPE tryFindByDataSetId(IDataSetId dataSetId)
    {
        if (dataSetId == null)
        {
            throw new IllegalArgumentException("Data set id cannot be null");
        }
        if (dataSetId instanceof DataSetCodeId)
        {
            DataSetCodeId codeId = (DataSetCodeId) dataSetId;
            return tryToFindDataSetByCode(codeId.getCode());
        } else if (dataSetId instanceof DataSetTechIdId)
        {
            DataSetTechIdId techIdId = (DataSetTechIdId) dataSetId;
            return getDataDAO().tryGetByTechId(new TechId(techIdId.getTechId()));
        } else
        {
            throw new IllegalArgumentException("Unsupported data set id: " + dataSetId);
        }
    }

    @Override
    public DataPE tryGetData()
    {
        return data;
    }

    @Override
    public DataPE getData()
    {
        // TODO 2010-04-12, CR: This should throw an exception if the external data is null
        // -- will that cause problems with clients?
        return data;
    }

    @Override
    public void loadByCode(String dataSetCode)
    {
        loadByCode(dataSetCode, true, false);
    }

    private void loadByCode(String dataSetCode, boolean withPropertyTypes, boolean lockForUpdate)
    {
        data =
                getDataDAO().tryToFindFullDataSetByCode(dataSetCode, withPropertyTypes,
                        lockForUpdate);
    }

    static final String DATA_SET_TYPE = "dataSetType";

    static final String PROPERTY_TYPES = "dataSetType.dataSetTypePropertyTypesInternal";

    @Override
    public void loadDataByTechId(TechId datasetId)
    {
        String[] connections =
                { PROPERTY_TYPES, DATA_SET_TYPE };
        data = getDataDAO().tryGetByTechId(datasetId, connections);
        if (data == null)
        {
            throw new UserFailureException(String.format("Data set with ID '%s' does not exist.",
                    datasetId));
        }
        HibernateUtils.initialize(data.tryGetSample());
        HibernateUtils.initialize(data.getDataSetType().getDataSetTypePropertyTypes());
    }

    @Override
    public void enrichWithParentsAndExperiment()
    {
        if (data != null)
        {
            enrichWithParentsAndExperiment(data);
        }
    }

    @Override
    public void enrichWithChildren()
    {
        if (data != null)
        {
            enrichWithChildren(data);
        }
    }

    @Override
    public final void enrichWithProperties()
    {
        if (data != null)
        {
            HibernateUtils.initialize(data.getProperties());
        }
    }

    @Override
    public void enrichWithContainedDataSets()
    {
        if (data != null && data.isContainer())
        {
            HibernateUtils.initialize(data.getContainedDataSets());
        }
    }

    @Override
    public void define(NewExternalData newData, SamplePE sample, SourceType sourceType)
    {
        assertAllowedSampleForDataSet(newData, sample);

        boolean isContainer = newData instanceof NewContainerDataSet;
        boolean isLink = newData instanceof NewLinkDataSet;
        if (isContainer)
        {
            define((NewContainerDataSet) newData, sourceType);
        } else if (isLink)
        {
            define((NewLinkDataSet) newData, sourceType);
        } else
        {
            define(newData, sourceType);
        }

        ExperimentPE experiment = sample.getExperiment();
        Date transactionTimeStamp = getTransactionTimeStamp();
        RelationshipUtils.setSampleForDataSet(data, sample, session, transactionTimeStamp);
        RelationshipUtils.setExperimentForDataSet(data, experiment, session, transactionTimeStamp);

        setParentDataSets(experiment, sample, newData);
    }

    private void assertAllowedSampleForDataSet(NewExternalData newData, SamplePE sample)
    {
        assert sample != null : "Undefined sample.";
        ExperimentPE experiment = sample.getExperiment();
        if (experiment == null)
        {
            if (dataSetTypeChecker.isDataSetTypeWithoutExperiment(newData.getDataSetType().getCode()))
            {
                if (sample.getSpace() == null)
                {
                    throw new UserFailureException("Data set can not be registered because sample '"
                            + sample.getSampleIdentifier() + "' is a shared sample.");
                } else if (sample.getDeletion() != null)
                {
                    throw new UserFailureException("Data set can not be registered because sample '"
                            + sample.getSampleIdentifier() + "' is in trash.");
                }
            } else
            {
                throw new UserFailureException("Data set can not be registered because no experiment "
                        + "found for sample '" + sample.getSampleIdentifier() + "'.");
            }
        } else if (experiment.getDeletion() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is in trash.");
        }
    }

    @Override
    public void define(NewExternalData newData, ExperimentPE experiment, SourceType sourceType)
    {
        assert experiment != null : "Undefined experiment.";

        boolean isContainer = newData instanceof NewContainerDataSet;
        boolean isLink = newData instanceof NewLinkDataSet;
        if (isContainer)
        {
            define((NewContainerDataSet) newData, sourceType);
        } else if (isLink)
        {
            define((NewLinkDataSet) newData, sourceType);
        } else
        {
            define(newData, sourceType);
        }

        RelationshipUtils.setExperimentForDataSet(data, experiment, session, getTransactionTimeStamp());
        setParentDataSets(experiment, null, newData);
    }

    private void setParentDataSets(ExperimentPE experiment, SamplePE sample, NewExternalData newData)
    {
        final List<String> parentDataSetCodes = newData.getParentDataSetCodes();
        final Set<DataPE> parentsToAdd = new HashSet<DataPE>();
        if (parentDataSetCodes != null)
        {
            for (String parentCode : parentDataSetCodes)
            {
                DataPE parent = this.getCache().getDataSets().get(parentCode);
                if (parent == null)
                {
                    parent = getData(parentCode, experiment, sample);
                    this.getCache().getDataSets().put(parentCode, parent);
                }
                parentsToAdd.add(parent);
            }
        }

        replaceParents(data, parentsToAdd, false);
    }

    @Override
    public void setContainedDataSets(ExperimentPE experimentOrNull, SamplePE sampleOrNull, NewContainerDataSet newData)
    {
        final List<String> containedDataSetCodes = newData.getContainedDataSetCodes();
        if (containedDataSetCodes != null)
        {
            for (String containedCode : containedDataSetCodes)
            {
                final DataPE contained = getData(containedCode, experimentOrNull, sampleOrNull);
                relationshipService.assignDataSetToContainer(session, contained, data);
            }
        }
    }

    private void define(NewExternalData newData, SourceType sourceType)
    {
        assert newData != null : "Undefined data.";
        final DataSetType dataSetType = newData.getDataSetType();
        assert dataSetType != null : "Undefined data set type.";
        final FileFormatType fileFormatType = newData.getFileFormatType();
        assert fileFormatType != null : "Undefined file format type.";
        final String location = newData.getLocation();
        assert location != null : "Undefined location.";
        final LocatorType locatorType = newData.getLocatorType();
        assert locatorType != null : "Undefined location type.";
        assert sourceType != null : "Undefined source type.";

        final ExternalDataPE externalData = new ExternalDataPE();

        externalData.setDataProducerCode(newData.getDataProducerCode());
        externalData.setProductionDate(newData.getProductionDate());
        externalData.setCode(newData.getCode());
        externalData.setDataSetType(getDataSetType(dataSetType));
        externalData.setDataSetKind(DataSetKind.PHYSICAL.name());
        externalData.setFileFormatType(getFileFomatType(fileFormatType));
        externalData.setComplete(newData.getComplete());
        externalData.setShareId(newData.getShareId());
        externalData.setLocation(location);
        externalData.setSize(newData.getSize());
        externalData.setSpeedHint(newData.getSpeedHint());
        externalData.setStorageFormatVocabularyTerm(tryToFindStorageFormatTerm(newData
                .getStorageFormat()));
        externalData.setLocatorType(tryToFindLocatorTypeByCode(locatorType));

        PersonPE registrator = tryToGetRegistrator(newData);
        externalData.setRegistrator(registrator);
        RelationshipUtils.updateModificationDateAndModifier(externalData, registrator, getTransactionTimeStamp());
        dataStore = tryToFindDataStoreByCode(newData.getDataStoreCode());
        externalData.setDataStore(dataStore);
        defineDataSetProperties(externalData,
                convertToDataSetProperties(newData.getDataSetProperties()));
        externalData.setDerived(sourceType == SourceType.DERIVED);

        data = externalData;
    }

    private DataStorePE tryToFindDataStoreByCode(String dataStoreCode)
    {
        DataStorePE dataStorePe = this.getCache().getDataStores().get(dataStoreCode);

        if (dataStorePe == null)
        {
            dataStorePe = getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
            this.getCache().getDataStores().put(dataStoreCode, dataStorePe);
        }
        return dataStorePe;
    }

    private LocatorTypePE tryToFindLocatorTypeByCode(final LocatorType locatorType)
    {
        LocatorTypePE locatorTypePe = this.getCache().getLocatorTypes().get(locatorType.getCode());
        if (locatorTypePe == null)
        {
            locatorTypePe = getLocatorTypeDAO().tryToFindLocatorTypeByCode(
                    locatorType.getCode());
            this.getCache().getLocatorTypes().put(locatorType.getCode(), locatorTypePe);

        }
        return locatorTypePe;
    }

    private void define(NewContainerDataSet newData, SourceType sourceType)
    {
        assert newData != null : "Undefined data.";
        final DataSetType dataSetType = newData.getDataSetType();
        assert dataSetType != null : "Undefined data set type.";
        assert sourceType != null : "Undefined source type.";

        final DataPE dataPE = new DataPE();

        dataPE.setDataProducerCode(newData.getDataProducerCode());
        dataPE.setProductionDate(newData.getProductionDate());
        dataPE.setCode(newData.getCode());
        dataPE.setDataSetType(getDataSetType(dataSetType));
        dataPE.setDataSetKind(DataSetKind.CONTAINER.name());
        PersonPE registrator = tryToGetRegistrator(newData);
        dataPE.setRegistrator(registrator);
        RelationshipUtils.updateModificationDateAndModifier(dataPE, registrator, getTransactionTimeStamp());
        dataStore = tryToFindDataStoreByCode(newData.getDataStoreCode());
        dataPE.setDataStore(dataStore);
        defineDataSetProperties(dataPE, convertToDataSetProperties(newData.getDataSetProperties()));
        dataPE.setDerived(sourceType == SourceType.DERIVED);

        data = dataPE;
    }

    private void define(NewLinkDataSet newData, SourceType sourceType)
    {
        assert newData != null : "Undefined data.";
        final DataSetType dataSetType = newData.getDataSetType();
        assert dataSetType != null : "Undefined data set type.";
        assert sourceType != null : "Undefined source type.";

        final LinkDataPE dataPE = new LinkDataPE();

        String code = newData.getExternalDataManagementSystemCode();
        ExternalDataManagementSystemPE externalDMS =
                getExternalDataManagementSystemDAO().tryToFindExternalDataManagementSystemByCode(
                        code);

        ContentCopyPE copy = new ContentCopyPE();
        copy.setExternalCode(newData.getExternalCode());
        copy.setExternalDataManagementSystem(externalDMS);
        if (externalDMS.getAddressType().equals(ExternalDataManagementSystemType.OPENBIS))
        {
            copy.setLocationType(LocationType.OPENBIS);
        } else
        {
            copy.setLocationType(LocationType.URL);
        }
        copy.setRegistrator(tryToGetRegistrator(newData));
        dataPE.setContentCopies(Collections.singleton(copy));

        dataPE.setDataProducerCode(newData.getDataProducerCode());
        dataPE.setProductionDate(newData.getProductionDate());
        dataPE.setCode(newData.getCode());
        dataPE.setDataSetType(getDataSetType(dataSetType));
        dataPE.setDataSetKind(DataSetKind.LINK.name());
        dataPE.setRegistrator(tryToGetRegistrator(newData));
        dataStore = tryToFindDataStoreByCode(newData.getDataStoreCode());
        dataPE.setDataStore(dataStore);
        defineDataSetProperties(dataPE, convertToDataSetProperties(newData.getDataSetProperties()));
        dataPE.setDerived(sourceType == SourceType.DERIVED);

        data = dataPE;
    }

    private PersonPE tryToGetRegistrator(NewExternalData newData)
    {
        String userId = newData.getUserId();
        if (userId != null)
        {
            return getPersonDAO().tryFindPersonByUserId(userId);
        }
        String userEMail = newData.getUserEMail();
        if (userEMail == null)
        {
            return null;
        }
        List<PersonPE> persons = getPersonDAO().listPersons();
        for (PersonPE person : persons)
        {
            if (userEMail.equalsIgnoreCase(person.getEmail()))
            {
                return person;
            }
        }
        return null;
    }

    private VocabularyTermPE tryToFindStorageFormatTerm(StorageFormat storageFormat)
    {
        IVocabularyDAO vocabularyDAO = getVocabularyDAO();

        VocabularyPE vocabulary = this.getCache().getStorageFormatVocabulary();

        if (vocabulary == null)
        {
            vocabulary = vocabularyDAO.tryFindVocabularyByCode(StorageFormat.VOCABULARY_CODE);
            this.getCache().setStorageFormatVocabulary(vocabulary);
        }
        Set<VocabularyTermPE> terms = vocabulary.getTerms();
        for (VocabularyTermPE term : terms)
        {
            if (storageFormat.getCode().equals(term.getCode()))
            {
                return term;
            }
        }
        return null;
    }

    private final DataSetTypePE getDataSetType(final DataSetType dataSetType)
    {
        final String dataSetTypeCode = dataSetType.getCode();

        DataSetTypePE dataSetTypeOrNull = this.getCache().getDataSetTypes().get(dataSetTypeCode);

        if (dataSetTypeOrNull == null)
        {
            dataSetTypeOrNull = getDataSetTypeDAO().tryToFindDataSetTypeByCode(dataSetTypeCode);
            this.getCache().getDataSetTypes().put(dataSetTypeCode, dataSetTypeOrNull);
        }

        if (dataSetTypeOrNull == null)
        {
            throw UserFailureException.fromTemplate("There is no data set type with code '%s'",
                    dataSetTypeCode);
        }
        return dataSetTypeOrNull;
    }

    private final FileFormatTypePE getFileFomatType(final FileFormatType fileFormatType)
    {
        final String fileFormatTypeCode = fileFormatType.getCode();

        FileFormatTypePE fileFormatTypeOrNull = this.getCache().getFileFormatTypes().get(fileFormatTypeCode);

        if (fileFormatTypeOrNull == null)
        {
            fileFormatTypeOrNull =
                    getFileFormatTypeDAO().tryToFindFileFormatTypeByCode(fileFormatTypeCode);
            this.getCache().getFileFormatTypes().put(fileFormatTypeCode, fileFormatTypeOrNull);
        }
        if (fileFormatTypeOrNull == null)
        {
            throw UserFailureException.fromTemplate("There is no file format type with code '%s'",
                    fileFormatTypeCode);
        }
        return fileFormatTypeOrNull;
    }

    private final DataPE getData(final String dataSetCode, ExperimentPE experiment, SamplePE sample)
    {
        assert dataSetCode != null : "Unspecified parent data set code.";

        DataPE result = tryToFindDataSetByCode(dataSetCode);
        if (result == null)
        {
            throw UserFailureException.fromTemplate("Unknown data set code '%s'", dataSetCode);
        }
        return result;
    }

    private DataPE tryToFindDataSetByCode(final String dataSetCode)
    {
        DataPE dataSet = this.getCache().getDataSets().get(dataSetCode);
        if (dataSet == null)
        {
            dataSet = getDataDAO().tryToFindDataSetByCode(dataSetCode);
            this.getCache().getDataSets().put(dataSetCode, dataSet);
        }
        return dataSet;
    }

    @Override
    public void save() throws UserFailureException
    {
        assert data != null : "Undefined external data.";

        IDataDAO dataDAO = getDataDAO();
        dataDAO.createDataSet(data, findPerson());
        entityPropertiesConverter.checkMandatoryProperties(data.getProperties(),
                data.getDataSetType(), this.getCache().getEntityTypePropertyTypes());
    }

    @Override
    public void addPropertiesToDataSet(String dataSetCode, List<NewProperty> properties)
    {
        loadByCode(dataSetCode);
        updatePropertiesPreservingExisting(properties);
        entityPropertiesConverter.checkMandatoryProperties(data.getProperties(),
                data.getDataSetType(), this.getCache().getEntityTypePropertyTypes());
        validateAndSave();
    }

    private void updatePropertiesPreservingExisting(List<NewProperty> properties)
    {
        final Set<DataSetPropertyPE> existingProperties = data.getProperties();
        Set<String> propertyUpdatesCodes =
                extractPropertyCodesToUpdate(properties, existingProperties);
        List<NewProperty> propertyUpdates =
                extractNewPropertiesToUpdate(properties, propertyUpdatesCodes);
        final DataSetTypePE type = data.getDataSetType();
        final PersonPE registrator = findPerson();
        data.setProperties(entityPropertiesConverter.updateProperties(existingProperties, type,
                Arrays.asList(convertToDataSetProperties(propertyUpdates)), registrator,
                propertyUpdatesCodes));
    }

    private List<NewProperty> extractNewPropertiesToUpdate(List<NewProperty> properties,
            Set<String> propertiesToUpdate)
    {
        List<NewProperty> newPropertiesToUpdate = new ArrayList<NewProperty>();
        for (NewProperty np : properties)
        {
            if (propertiesToUpdate.contains(np.getPropertyCode()))
            {
                newPropertiesToUpdate.add(np);
            }
        }
        return newPropertiesToUpdate;
    }

    private Set<String> extractPropertyCodesToUpdate(List<NewProperty> properties,
            final Set<DataSetPropertyPE> existingProperties)
    {
        Set<String> propertiesToUpdate = new HashSet<String>();
        for (NewProperty np : properties)
        {
            propertiesToUpdate.add(np.getPropertyCode());
        }
        for (DataSetPropertyPE ep : existingProperties)
        {
            propertiesToUpdate.remove(ep.getEntityTypePropertyType().getPropertyType().getCode());
        }
        return propertiesToUpdate;
    }

    @Override
    public void assignDataSetToSampleAndExperiment(DataPE dataSet, SamplePE sample, ExperimentPE experiment)
    {
        super.assignDataSetToSampleAndExperiment(dataSet, sample, experiment);
    }

    @Override
    public void update(DataSetUpdatesDTO updates)
    {
        loadDataByTechId(updates.getDatasetId());
        if (updates.getVersion() != data.getVersion())
        {
            throwModifiedEntityException("Data set");
        }
        final SampleIdentifier sampleIdentifierOrNull = updates.getSampleIdentifierOrNull();
        if (sampleIdentifierOrNull != null)
        {
            // update sample and indirectly experiment
            updateSample(data, sampleIdentifierOrNull);
        } else if (updates.getExperimentIdentifierOrNull() != null)
        {
            updateExperiment(data, updates.getExperimentIdentifierOrNull());
        }

        setParents(data, asListOrNull(updates.getModifiedParentDatasetCodesOrNull()));
        setMetaprojects(data, updates.getMetaprojectsOrNull());
        updateContainer(updates.getModifiedContainerDatasetCodeOrNull());
        updateComponents(updates.getModifiedContainedDatasetCodesOrNull());
        updateFileFormatType(data, updates.getFileFormatTypeCode());
        updateProperties(data.getEntityType(), updates.getProperties(), extractPropertiesCodes(updates.getProperties()), data, data);

        entityPropertiesConverter.checkMandatoryProperties(data.getProperties(),
                data.getDataSetType(), this.getCache().getEntityTypePropertyTypes());

        validateAndSave();
    }

    private List<String> asListOrNull(String[] arrayOrNull)
    {
        if (arrayOrNull == null)
        {
            return null;
        } else
        {
            return Arrays.asList(arrayOrNull);
        }
    }

    private void validateAndSave()
    {
        try
        {
            getDataDAO().validateAndSaveUpdatedEntity(data);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Data Set '%s'", data.getCode()));
        }
    }

    private void updateContainer(String modifiedContainerDatasetCodeOrNull)
    {
        if (modifiedContainerDatasetCodeOrNull == null)
        {
            return; // container data set was not changed
        } else
        {
            updateContainers(data, modifiedContainerDatasetCodeOrNull);
        }
    }

    private void updateComponents(String[] modifiedContainedDatasetCodesOrNull)
    {
        if (modifiedContainedDatasetCodesOrNull == null)
        {
            return; // contained data sets were not changed
        } else if (modifiedContainedDatasetCodesOrNull.length > 0 && false == data.isContainer())
        {
            throw new UserFailureException("Data set '" + data.getCode()
                    + " is not a container data set, and cannot contain other data sets.");
        } else
        {
            final List<DataPE> currentComponents =
                    new ArrayList<DataPE>(data.getContainedDataSets());
            removeComponents(currentComponents);

            final Set<String> currentCodes = extractCodes(currentComponents);
            final Set<String> newCodes = asSet(asListOrNull(modifiedContainedDatasetCodesOrNull));

            // quick check for direct cycle
            final Set<String> brandNewCodes = new HashSet<String>(newCodes);
            brandNewCodes.removeAll(currentCodes);

            validateContainerRelationshipGraph(brandNewCodes);
            final Set<DataPE> newComponents = findDataSetsByCodes(newCodes);
            addComponents(newComponents);
        }
    }

    private void validateContainerRelationshipGraph(Collection<String> componentCodes)
    {
        if (componentCodes.isEmpty())
        {
            return;
        } else if (componentCodes.contains(data.getCode()))
        {
            throw new UserFailureException(
                    "Data set '"
                            + data.getCode()
                            + "' cannot contain itself as a component neither directly nor via subordinate components.");
        }

        final Set<DataPE> components = findDataSetsByCodes(componentCodes);
        for (DataPE componentDataSet : components)
        {
            if (componentDataSet.isContainer())
            {
                List<DataPE> containedDataSets = componentDataSet.getContainedDataSets();
                validateContainerRelationshipGraph(Code.extractCodes(containedDataSets));
            }
        }

    }

    private void addComponents(Collection<DataPE> componentsToAdd)
    {
        for (DataPE component : componentsToAdd)
        {
            relationshipService.assignDataSetToContainer(session, component, data);
        }
    }

    private void removeComponents(Collection<DataPE> componentsToRemove)
    {
        for (DataPE component : componentsToRemove)
        {
            relationshipService.removeDataSetFromContainer(session, component, data);
        }
    }

    private final void defineDataSetProperties(final DataPE dataSet,
            final IEntityProperty[] newProperties)
    {
        final String dataSetTypeCode = dataSet.getDataSetType().getCode();
        final List<DataSetPropertyPE> properties =
                entityPropertiesConverter.convertProperties(newProperties, dataSetTypeCode,
                        findPerson());
        for (final DataSetPropertyPE property : properties)
        {
            dataSet.addProperty(property);
        }
    }

    private static IEntityProperty[] convertToDataSetProperties(List<NewProperty> list)
    {
        IEntityProperty[] result = new IEntityProperty[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            result[i] = convertProperty(list.get(i));
        }
        return result;
    }

    private static IEntityProperty convertProperty(NewProperty newProperty)
    {
        IEntityProperty result = new EntityProperty();
        result.setValue(newProperty.getValue());
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(newProperty.getPropertyCode());
        result.setPropertyType(propertyType);
        return result;
    }

    @Override
    public void updateStatuses(List<String> dataSetCodes, DataSetArchivingStatus newStatus,
            boolean newPresentInArchive)
    {
        getDataDAO().updateDataSetStatuses(dataSetCodes, newStatus, newPresentInArchive);
    }

    @Override
    public void updateSizes(Map<String, Long> sizeMap)
    {
        getDataDAO().updateSizes(sizeMap);
    }

    @Override
    public boolean compareAndSetDataSetStatus(DataSetArchivingStatus oldStatus,
            DataSetArchivingStatus newStatus, boolean newPresentInArchive)
    {
        if (data == null || data.isExternalData() == false)
        {
            return false;
        }
        final ExternalDataPE externalData = data.tryAsExternalData();
        if (externalData.getStatus() != oldStatus)
        {
            return false;
        }
        updateStatuses(Arrays.asList(data.getCode()), newStatus, newPresentInArchive);
        return true;
    }

    @Override
    public void updateManagedProperty(IManagedProperty managedProperty)
    {
        final Set<DataSetPropertyPE> existingProperties = data.getProperties();
        final DataSetTypePE type = data.getDataSetType();
        final PersonPE registrator = findPerson();
        data.setProperties(entityPropertiesConverter.updateManagedProperty(existingProperties,
                type, managedProperty, registrator));
    }

    @Override
    public void setStorageConfirmed()
    {
        ExternalDataPE externalData = data.tryAsExternalData();
        if (null != externalData)
        {
            externalData.setStorageConfirmation(true);
        }
    }

    @Override
    public boolean isStorageConfirmed()
    {
        ExternalDataPE externalData = data.tryAsExternalData();
        if (null != externalData)
        {
            return externalData.isStorageConfirmation();
        }
        return true;
    }

    private DataSetRegistrationCache cache;

    @Override
    public void setCache(DataSetRegistrationCache cache)
    {
        this.cache = cache;
    }

    @Override
    public DataSetRegistrationCache getCache()
    {
        if (this.cache == null)
        {
            this.cache = new DataSetRegistrationCache();
        }
        return this.cache;
    }
}
