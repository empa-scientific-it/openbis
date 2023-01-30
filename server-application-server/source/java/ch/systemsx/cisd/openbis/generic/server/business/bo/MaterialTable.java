/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;

/**
 * The only productive implementation of {@link IMaterialTable}.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialTable extends AbstractMaterialBusinessObject implements IMaterialTable
{
    private List<MaterialPE> materials;

    private boolean dataChanged;

    public MaterialTable(final IDAOFactory daoFactory, final Session session,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, entityInformationProvider, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
    }

    @Private
    // for tests only
    MaterialTable(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter, List<MaterialPE> materials,
            boolean dataChanged, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, entityPropertiesConverter, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
        this.materials = materials;
        this.dataChanged = dataChanged;
    }

    @Override
    public final List<MaterialPE> getMaterials()
    {
        assert materials != null : "Materials have not been loaded.";
        return materials;
    }

    @Override
    public void add(List<NewMaterial> newMaterials, MaterialTypePE materialTypePE)
    {
        assert newMaterials != null : "New materials undefined.";
        assert materialTypePE != null : "Material type undefined.";
        if (materials == null)
        {
            materials = new ArrayList<MaterialPE>();
        }
        setBatchUpdateMode(true);
        for (NewMaterial newMaterial : newMaterials)
        {
            materials.add(createMaterial(newMaterial, materialTypePE));
        }
        setBatchUpdateMode(false);
        dataChanged = true;
    }

    @Override
    public void add(List<NewMaterialWithType> newMaterials, Map<String, MaterialTypePE> materialTypePE)
    {
        assert newMaterials != null : "New materials undefined.";
        assert materialTypePE != null : "Material type undefined.";
        if (materials == null)
        {
            materials = new ArrayList<MaterialPE>();
        }
        setBatchUpdateMode(true);
        for (NewMaterialWithType newMaterial : newMaterials)
        {
            materials.add(createMaterial(newMaterial.getMaterial(), materialTypePE.get(newMaterial.getType())));
        }
        setBatchUpdateMode(false);
        dataChanged = true;
    }

    @Override
    public void save()
    {
        assert materials != null : "Materials have not been loaded.";
        assert dataChanged == true : "Data not changed";
        try
        {
            getMaterialDAO().createOrUpdateMaterials(materials);
            checkBusinessRules();
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("One of materials"));
        }
        dataChanged = false;
    }

    private void checkBusinessRules()
    {
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        for (MaterialPE m : materials)
        {
            entityPropertiesConverter.checkMandatoryProperties(m.getProperties(),
                    m.getMaterialType(), cache);
        }
    }

    private MaterialPE createMaterial(NewMaterial newMaterial, MaterialTypePE materialTypePE)
    {
        final MaterialPE material = new MaterialPE();
        material.setCode(newMaterial.getCode());
        material.setRegistrator(findPerson());
        material.setMaterialType(materialTypePE);
        if (newMaterial.getProperties().length > 0)
        {
            defineMaterialProperties(material, newMaterial.getProperties());
        }
        return material;
    }

    private final void defineMaterialProperties(final MaterialPE material,
            final IEntityProperty[] materialProperties)
    {
        final String materialTypeCode = material.getMaterialType().getCode();
        final List<MaterialPropertyPE> properties =
                entityPropertiesConverter.convertProperties(materialProperties, materialTypeCode,
                        material.getRegistrator());
        for (final MaterialPropertyPE materialProperty : properties)
        {
            material.addProperty(materialProperty);
        }
    }

    // -----------------

    @Override
    public void update(List<MaterialUpdateDTO> materialsUpdate)
    {
        setBatchUpdateMode(true);

        // load all materials to be updated in one query
        final List<Long> materialIds = new ArrayList<Long>();
        for (MaterialUpdateDTO materialUpdate : materialsUpdate)
        {
            materialIds.add(materialUpdate.getMaterialId().getId());
        }
        materials = getMaterialDAO().listMaterialsById(materialIds);
        Map<Long, MaterialPE> materialsById = new HashMap<Long, MaterialPE>();
        for (MaterialPE material : materials)
        {
            materialsById.put(material.getId(), material);
        }

        // update
        for (MaterialUpdateDTO materialUpdate : materialsUpdate)
        {
            MaterialPE material = materialsById.get(materialUpdate.getMaterialId().getId());
            update(material, materialUpdate);
        }

        setBatchUpdateMode(false);

        dataChanged = true;
    }

    private void update(MaterialPE material, MaterialUpdateDTO materialUpdate)
    {
        if (materialUpdate.getVersion().equals(material.getModificationDate()) == false)
        {
            throwModifiedEntityException("Material");
        }
        Set<MaterialPropertyPE> newProperties =
                calculateNewProperties(material, materialUpdate.getProperties());
        material.setProperties(newProperties);
    }

    private Set<MaterialPropertyPE> calculateNewProperties(MaterialPE material,
            List<IEntityProperty> propertiesToUpdate)
    {
        final Set<MaterialPropertyPE> existingProperties = material.getProperties();
        final MaterialTypePE type = material.getMaterialType();
        final PersonPE registrator = findPerson();
        final Set<String> propertiesToUpdateNames = extractCodes(propertiesToUpdate);
        return entityPropertiesConverter.updateProperties(existingProperties, type,
                propertiesToUpdate, registrator, propertiesToUpdateNames);
    }

    private static Set<String> extractCodes(List<IEntityProperty> propertiesToUpdate)
    {
        Set<String> names = new HashSet<String>();
        for (IEntityProperty p : propertiesToUpdate)
        {
            names.add(p.getPropertyType().getCode());
        }
        return names;
    }

    @Override
    public void deleteByTechIds(List<TechId> materialIds, String reason)
            throws UserFailureException
    {
        try
        {
            getSessionFactory().getCurrentSession().flush();
            getSessionFactory().getCurrentSession().clear();
            getMaterialDAO().delete(materialIds, session.tryGetPerson(), reason);
        } catch (final DataAccessException ex)
        {
            String errorSubject = "Material";

            // when deletion fails because of a constraint violation
            // the permId of the problematic material is kept in the error message
            if (ex.getCause() != null && (ex instanceof DataIntegrityViolationException))
            {
                String materialPermId = ex.getCause().getMessage();
                errorSubject += " '" + materialPermId + "'";
            }
            throwException(ex, errorSubject, EntityKind.MATERIAL);
        }
    }

}
