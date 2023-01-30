/*
 * Copyright 2008 ETH Zuerich, CISD
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
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.MaterialDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.MaterialCodeAndTypeCodeId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.MaterialTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The unique {@link IMaterialBO} implementation.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialBO extends AbstractMaterialBusinessObject implements IMaterialBO
{
    private MaterialPE material;

    private boolean dataChanged;

    private EntityHistoryCreator historyCreator;

    public MaterialBO(final IDAOFactory daoFactory, final Session session,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService, EntityHistoryCreator historyCreator)
    {
        super(daoFactory, session, entityInformationProvider, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
        this.historyCreator = historyCreator;
    }

    @Override
    public MaterialPE tryFindByMaterialId(IMaterialId materialId)
    {
        if (materialId == null)
        {
            throw new IllegalArgumentException("Material id cannot be null");
        }
        if (materialId instanceof MaterialCodeAndTypeCodeId)
        {
            MaterialCodeAndTypeCodeId codeAndTypeCodeId = (MaterialCodeAndTypeCodeId) materialId;
            MaterialIdentifier identifier =
                    new MaterialIdentifier(codeAndTypeCodeId.getCode(),
                            codeAndTypeCodeId.getTypeCode());
            return getMaterialDAO().tryFindMaterial(identifier);
        } else if (materialId instanceof MaterialTechIdId)
        {
            MaterialTechIdId techIdId = (MaterialTechIdId) materialId;
            return getMaterialDAO().tryGetByTechId(new TechId(techIdId.getTechId()));
        } else
        {
            throw new IllegalArgumentException("Unsupported material id: " + materialId);
        }
    }

    @Override
    public void loadDataByTechId(TechId materialId)
    {
        material = getMaterialById(materialId);
        dataChanged = false;
        HibernateUtils.initialize(material.getMaterialType().getMaterialTypePropertyTypes());
    }

    @Override
    public void loadByMaterialIdentifier(MaterialIdentifier identifier)
    {
        material = getMaterialDAO().tryFindMaterial(identifier);
        if (material == null)
        {
            throw new UserFailureException(String.format(
                    "Material with identifier '%s' does not exist.", identifier));
        }
        dataChanged = false;
    }

    @Override
    public final void enrichWithProperties()
    {
        if (material != null)
        {
            HibernateUtils.initialize(material.getProperties());
        }
    }

    @Override
    public void save() throws UserFailureException
    {
        assert dataChanged : "Data not changed";
        try
        {
            getMaterialDAO().createOrUpdateMaterials(Collections.singletonList(material));
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Material '%s'", material.getCode()));
        }
        checkBusinessRules();
        dataChanged = false;
    }

    private void checkBusinessRules()
    {
        entityPropertiesConverter.checkMandatoryProperties(material.getProperties(),
                material.getMaterialType());
    }

    @Override
    public void update(MaterialUpdateDTO materialUpdate)
    {
        loadDataByTechId(materialUpdate.getMaterialId());
        if (materialUpdate.getVersion().equals(material.getModificationDate()) == false)
        {
            throwModifiedEntityException("Material");
        }
        setMetaprojects(material, materialUpdate.getMetaprojectsOrNull());
        List<IEntityProperty> properties = materialUpdate.getProperties();
        updateProperties(material.getMaterialType(), properties, extractPropertiesCodes(properties), material, material, null);
        dataChanged = true;
    }

    @Override
    public MaterialPE getMaterial()
    {
        return material;
    }

    @Override
    public void deleteByTechId(TechId materialId, String reason)
    {
        loadDataByTechId(materialId);
        try
        {
            List<Long> idsToDelete = Collections.singletonList(material.getId());
            String content = historyCreator.apply(getSessionFactory().getCurrentSession(), idsToDelete,
                    MaterialDAO.sqlPropertyHistory, null, MaterialDAO.sqlAttributesHistory, null,
                    null, session.tryGetPerson());

            getMaterialDAO().delete(material);
            getEventDAO().persist(createDeletionEvent(material, session.tryGetPerson(), reason, content));
        } catch (final DataAccessException ex)
        {
            throwException(ex, material.getPermId(), EntityKind.MATERIAL);
        }
    }

    public static EventPE createDeletionEvent(MaterialPE material, PersonPE registrator,
            String reason, String content)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.MATERIAL);
        event.setIdentifiers(Collections.singletonList(material.getCode()));
        event.setDescription(material.getPermId());
        event.setReason(reason);
        event.setRegistrator(registrator);
        event.setContent(content);
        return event;
    }

    @Override
    public void updateManagedProperty(IManagedProperty managedProperty)
    {
        final Set<MaterialPropertyPE> existingProperties = material.getProperties();
        final MaterialTypePE type = material.getMaterialType();
        final PersonPE registrator = findPerson();
        material.setProperties(entityPropertiesConverter.updateManagedProperty(existingProperties,
                type, managedProperty, registrator));
        dataChanged = true;
    }
}
