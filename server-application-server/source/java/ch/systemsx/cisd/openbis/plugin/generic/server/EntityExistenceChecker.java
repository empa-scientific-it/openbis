/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Checker of entities. Asks DAO for existing entities and chaches already asked entities.
 * 
 * @author Franz-Josef Elmer
 */
class EntityExistenceChecker
{
    private static abstract class AbstractExistenceManager<T, E>
    {
        private static enum Status
        {
            KNOWN, UNKNOWN;
        }

        private final Map<T, Status> identifierToStatusMap = new HashMap<T, Status>();

        private final Map<T, E> identifierToEntityMap = new HashMap<T, E>();

        private final String name;

        private final Set<String> errors;

        public AbstractExistenceManager(Set<String> errors, String name)
        {
            this.errors = errors;
            this.name = name;
        }

        public void add(T identifier)
        {
            identifierToStatusMap.put(identifier, Status.KNOWN);
        }

        public E tryGet(T identifier)
        {
            return identifierToEntityMap.get(identifier);
        }

        public boolean exists(T identifier)
        {
            int size = errors.size();
            Status status = identifierToStatusMap.get(identifier);
            if (Status.KNOWN.equals(status))
            {
                return true;
            }
            if (status == null)
            {
                E entity = tryGetEntity(identifier);
                if (entity != null)
                {
                    identifierToStatusMap.put(identifier, Status.KNOWN);
                    identifierToEntityMap.put(identifier, entity);
                    return true;
                }
                identifierToStatusMap.put(identifier, Status.UNKNOWN);
            }
            if (size == errors.size())
            {
                // add an error only if tryGetEntity() hasn't added one.
                errors.add("Unknown " + name + ": " + identifier);
            }
            return false;
        }

        protected abstract E tryGetEntity(T identifier);
    }

    private final IDAOFactory daoFactory;

    private final AbstractExistenceManager<ExperimentIdentifier, ExperimentPE> experimentExistenceManager;

    private final AbstractExistenceManager<MaterialIdentifier, MaterialPE> materialExistenceManager;

    private final AbstractExistenceManager<SampleIdentifier, SamplePE> sampleExistenceManager;

    private final AbstractExistenceManager<SpaceIdentifier, SpacePE> spaceExistenceManager;

    private final Map<String, Map<String, PropertyTypePE>> materialTypeToPropertyTypesMap =
            new HashMap<String, Map<String, PropertyTypePE>>();

    private final Map<String, Map<String, PropertyTypePE>> sampleTypeToPropertyTypesMap =
            new HashMap<String, Map<String, PropertyTypePE>>();

    private final Set<String> errors;

    EntityExistenceChecker(final IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
        this.errors = new TreeSet<String>();
        experimentExistenceManager =
                new AbstractExistenceManager<ExperimentIdentifier, ExperimentPE>(errors,
                        "experiment")
                    {
                        @Override
                        protected ExperimentPE tryGetEntity(ExperimentIdentifier identifier)
                        {
                            ProjectPE project =
                                    daoFactory.getProjectDAO().tryFindProject(
                                            identifier.getSpaceCode(), identifier.getProjectCode());
                            if (project == null)
                            {
                                errors.add("Unknown experiment because of unknown project: "
                                        + identifier);
                                return null;
                            }
                            ExperimentPE experiment =
                                    daoFactory.getExperimentDAO().tryFindByCodeAndProject(project,
                                            identifier.getExperimentCode());
                            return experiment;
                        }
                    };
        materialExistenceManager =
                new AbstractExistenceManager<MaterialIdentifier, MaterialPE>(errors, "material")
                    {
                        @Override
                        protected MaterialPE tryGetEntity(MaterialIdentifier identifier)
                        {
                            return daoFactory.getMaterialDAO().tryFindMaterial(identifier);
                        }
                    };
        sampleExistenceManager =
                new AbstractExistenceManager<SampleIdentifier, SamplePE>(errors, "sample")
                    {

                        @Override
                        protected SamplePE tryGetEntity(SampleIdentifier identifier)
                        {
                            String sampleCode = identifier.getSampleCode();
                            ISampleDAO sampleDAO = daoFactory.getSampleDAO();
                            if (identifier.isSpaceLevel())
                            {
                                SpaceIdentifier spaceLevel = identifier.getSpaceLevel();
                                if (spaceExistenceManager.exists(spaceLevel) == false)
                                {
                                    return null;
                                }
                                return sampleDAO.tryFindByCodeAndSpace(sampleCode,
                                        spaceExistenceManager.tryGet(spaceLevel));
                            }
                            if (identifier.isProjectLevel())
                            {
                                ProjectIdentifier projectIdentifier = identifier.getProjectLevel();
                                IProjectDAO projectDAO = daoFactory.getProjectDAO();
                                String spaceCode = projectIdentifier.getSpaceCode();
                                String projectCode = projectIdentifier.getProjectCode();
                                ProjectPE project = projectDAO.tryFindProject(spaceCode, projectCode);
                                if (project == null)
                                {
                                    return null;
                                }
                                return sampleDAO.tryfindByCodeAndProject(sampleCode, project);
                            }
                            return sampleDAO.tryFindByCodeAndDatabaseInstance(sampleCode);
                        }
                    };
        spaceExistenceManager =
                new AbstractExistenceManager<SpaceIdentifier, SpacePE>(errors, "space")
                    {
                        @Override
                        protected SpacePE tryGetEntity(SpaceIdentifier identifier)
                        {
                            return daoFactory.getSpaceDAO().tryFindSpaceByCode(identifier.getSpaceCode());
                        }
                    };
    }

    public List<String> getErrors()
    {
        return new ArrayList<String>(errors);
    }

    /**
     * Checks specified materials. An error is added if
     * <ul>
     * <li>specified material type is not known
     * <li>materials have a property which is not assigned to specified material type.
     * </ul>
     */
    void checkNewMaterials(List<NewMaterialsWithTypes> newMaterialsWithType)
    {
        for (NewMaterialsWithTypes newMaterialsWithTypes : newMaterialsWithType)
        {
            MaterialType materialType = newMaterialsWithTypes.getEntityType();
            assertMaterialTypeExists(materialType);
            List<NewMaterial> newMaterials = newMaterialsWithTypes.getNewEntities();
            for (NewMaterial newMaterial : newMaterials)
            {
                IEntityProperty[] properties = newMaterial.getProperties();
                assertValidPropertyTypes(materialType, materialTypeToPropertyTypesMap, properties);
                String code = newMaterial.getCode();
                materialExistenceManager.add(new MaterialIdentifier(code, materialType.getCode()));
            }
        }
    }

    private void assertMaterialTypeExists(MaterialType materialType)
    {
        String materialTypeCode = materialType.getCode();
        if (materialTypeToPropertyTypesMap.containsKey(materialTypeCode) == false)
        {
            MaterialTypePE type =
                    (MaterialTypePE) daoFactory.getEntityTypeDAO(EntityKind.MATERIAL)
                            .tryToFindEntityTypeByCode(materialTypeCode);
            if (type == null)
            {
                errors.add("Unknown material type: " + materialTypeCode);
                return;
            }
            addPropertyTypesTo(materialTypeToPropertyTypesMap, materialTypeCode,
                    type.getMaterialTypePropertyTypes());
        }
    }

    /**
     * Checks specified samples. An error is added if
     * <ul>
     * <li>specified sample type is not known,
     * <li>a sample has a property which is not assigned to the specified sample type,
     * <li>a sample has an unknown property of data type MATERAL,
     * <li>a one sample is linked to an unknown experiment or an unknown container.
     * </ul>
     * Note, that the new samples are stored in the cache as known samples. Thus, they can be referred as container samples in a second call of this
     * method.
     */
    void checkNewSamples(List<NewSamplesWithTypes> newSamplesWithType)
    {
        for (NewSamplesWithTypes newSamplesWithTypes : newSamplesWithType)
        {
            SampleType sampleType = newSamplesWithTypes.getEntityType();
            assertSampleTypeExists(sampleType);
            List<NewSample> newSamples = newSamplesWithTypes.getNewEntities();
            for (NewSample newSample : newSamples)
            {
                IdentifersExtractor extractor = new IdentifersExtractor(newSample);
                ExperimentIdentifier experimentIdentifier =
                        extractor.getExperimentIdentifierOrNull();
                if (experimentIdentifier != null)
                {
                    experimentExistenceManager.exists(experimentIdentifier);
                }
                String containerIdentifier = newSample.getContainerIdentifierForNewSample();
                if (containerIdentifier != null)
                {
                    String defaultSpaceIdentifier = newSample.getDefaultSpaceIdentifier();
                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(containerIdentifier,
                                    defaultSpaceIdentifier);

                    sampleExistenceManager.exists(sampleIdentifier);
                }
                sampleExistenceManager.add(extractor.getNewSampleIdentifier());
                IEntityProperty[] properties = newSample.getProperties();
                assertValidPropertyTypes(sampleType, sampleTypeToPropertyTypesMap, properties);
            }
        }
    }

    private void assertSampleTypeExists(SampleType sampleType)
    {
        String sampleTypeCode = sampleType.getCode();
        if (sampleTypeToPropertyTypesMap.containsKey(sampleTypeCode) == false)
        {
            SampleTypePE type =
                    daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
            if (type == null)
            {
                errors.add("Unknown sample type: " + sampleTypeCode);
                return;
            }
            addPropertyTypesTo(sampleTypeToPropertyTypesMap, sampleTypeCode,
                    type.getSampleTypePropertyTypes());
        }
    }

    private void addPropertyTypesTo(
            Map<String, Map<String, PropertyTypePE>> entityTypeToPropertyTypesMap, String typeCode,
            Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes)
    {
        Map<String, PropertyTypePE> propertyTypes = new HashMap<String, PropertyTypePE>();
        for (EntityTypePropertyTypePE entityTypePropertyType : entityTypePropertyTypes)
        {
            PropertyTypePE propertyType = entityTypePropertyType.getPropertyType();
            propertyTypes.put(propertyType.getCode(), propertyType);
        }
        entityTypeToPropertyTypesMap.put(typeCode, propertyTypes);
    }

    private void assertValidPropertyTypes(EntityType entityType,
            Map<String, Map<String, PropertyTypePE>> entityTypeToPropertyTypesMap,
            IEntityProperty[] properties)
    {
        String entityTypeCode = entityType.getCode();
        Map<String, PropertyTypePE> propertyTypes =
                entityTypeToPropertyTypesMap.get(entityTypeCode);
        if (propertyTypes == null)
        {
            return;
        }
        for (IEntityProperty property : properties)
        {
            String propertyTypeCode = getPropertyTypeCode(property);
            PropertyTypePE propertyTypePE = propertyTypes.get(propertyTypeCode);
            if (propertyTypePE == null)
            {
                String typeName = entityType instanceof SampleType ? "Sample" : "Material";
                errors.add(typeName + " type " + entityTypeCode + " has no property type "
                        + propertyTypeCode + " assigned.");
            } else if (propertyTypePE.getType().getCode().equals(DataTypeCode.MATERIAL))
            {
                String value = property.getStringValue();
                MaterialTypePE materialType = propertyTypePE.getMaterialType();
                try
                {
                    MaterialIdentifier materialIdentifier =
                            MaterialIdentifier.tryCreate(value, materialType);
                    if (materialIdentifier == null)
                    {
                        errors.add("Material identifier not in the form '<material code> (<material type code>)': "
                                + value);
                    } else
                    {
                        materialExistenceManager.exists(materialIdentifier);
                    }
                } catch (Exception ex)
                {
                    errors.add(ex.getMessage());
                }
            }
        }
    }

    private String getPropertyTypeCode(IEntityProperty property)
    {
        String code = property.getPropertyType().getCode().toUpperCase();
        int indexOfColon = code.indexOf(':');
        return indexOfColon < 0 ? code : code.substring(0, indexOfColon);
    }

}
