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

package ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.lemnik.eodsql.DataIterator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.AbstractLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaProjectWithEntityId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectCriteria;

/**
 * Fast DB operations on material table.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
@Friend(toClasses =
{ MaterialRecord.class, IMaterialListingQuery.class })
public class MaterialLister extends AbstractLister implements IMaterialLister
{

    //
    // Input
    //

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    //
    // Working interfaces
    //

    private final IMaterialListingQuery query;

    private final IEntityPropertiesEnricher propertiesEnricher;

    private final Long userId;

    public static IMaterialLister create(IDAOFactory daoFactory, String baseIndexURL, Long userId)
    {
        MaterialListerDAO dao = MaterialListerDAO.create(daoFactory);
        SecondaryEntityDAO referencedEntityDAO = SecondaryEntityDAO.create(daoFactory);

        return create(dao, referencedEntityDAO, baseIndexURL, userId);
    }

    static IMaterialLister create(MaterialListerDAO dao, SecondaryEntityDAO referencedEntityDAO,
            String baseIndexURL, Long userId)
    {
        IMaterialListingQuery query = dao.getQuery();
        EntityPropertiesEnricher propertiesEnricher =
                new EntityPropertiesEnricher(query, dao.getPropertySetQuery());
        return new MaterialLister(dao.getDatabaseInstanceId(), dao.getDatabaseInstance(), query,
                propertiesEnricher, referencedEntityDAO, userId);
    }

    // For unit tests
    MaterialLister(final long databaseInstanceId, DatabaseInstance databaseInstance,
            final IMaterialListingQuery query, IEntityPropertiesEnricher propertiesEnricher,
            SecondaryEntityDAO referencedEntityDAO, Long userId)
    {
        super(referencedEntityDAO);
        assert query != null;

        this.databaseInstanceId = databaseInstanceId;
        this.databaseInstance = databaseInstance;
        this.query = query;
        this.propertiesEnricher = propertiesEnricher;
        this.userId = userId;
    }

    //
    // Listing
    //

    @Override
    public List<Material> list(ListMaterialCriteria criteria, boolean withProperties)
    {
        Long2ObjectMap<Material> materialMap = getMaterialsByCriteria(criteria);

        return convertAndEnrich(materialMap, withProperties);
    }

    @Override
    public List<Material> list(MetaprojectCriteria criteria, boolean withProperties)
    {
        Long2ObjectMap<Material> materialMap = getMaterialsByCriteria(criteria);

        return convertAndEnrich(materialMap, withProperties);
    }

    @Override
    public Collection<TechId> listMaterialsByMaterialProperties(Collection<TechId> materialIds)
    {
        DataIterator<Long> result =
                query.getMaterialIdsByMaterialProperties(new LongOpenHashSet(TechId.asLongs(materialIds)));
        return new HashSet<TechId>(TechId.createList(asList(result)));
    }

    /**
     * Remove from map elements wich are not in the identifiers list
     */
    private void filterCodesAndTypes(Long2ObjectMap<Material> materialMap,
            Collection<MaterialIdentifier> identifiers)
    {
        HashSet<String> identifiersMap = new HashSet<String>();
        for (MaterialIdentifier ident : identifiers)
        {
            identifiersMap.add(ident.toString()); // code (type)
        }

        List<Long> missingIds = new LinkedList<Long>();

        for (Long key : materialMap.keySet())
        {
            Material entry = materialMap.get(key);
            if (false == identifiersMap.contains(String.format("%s (%s)", entry.getCode(), entry
                    .getMaterialType().getCode())))
            {
                missingIds.add(key);
            }
        }

        for (Long key : missingIds)
        {
            materialMap.remove(key);
        }
    }

    private Long2ObjectMap<Material> getMaterialsByCriteria(ListMaterialCriteria criteria)
    {
        Collection<Long> materialIdsOrNull = criteria.tryGetMaterialIds();
        MaterialType materialTypeOrNull = criteria.tryGetMaterialType();
        Collection<MaterialIdentifier> identifiers = criteria.tryGetMaterialIdentifiers();

        if (materialTypeOrNull != null)
        {
            return asMaterials(getIteratorByType(materialTypeOrNull), materialTypeOrNull);
        } else if (materialIdsOrNull != null)
        {
            return asMaterials(getIteratorByIds(materialIdsOrNull), null);
        } else if (identifiers != null)
        {
            return getMaterialsByIndentifiers(identifiers);
        } else
        {
            throw new IllegalArgumentException(
                    "At least one of the three criterias should be not null.");
        }
    }

    private Long2ObjectMap<Material> getMaterialsByCriteria(MetaprojectCriteria criteria)
    {
        return asMaterials(getIteratorByMetaprojectId(criteria.getMetaprojectId()), null);
    }

    private Long2ObjectMap<Material> getMaterialsByIndentifiers(
            Collection<MaterialIdentifier> identifiers)
    {
        // extract array of codes
        String[] materialCodes =
                CollectionUtils.collect(identifiers, new Transformer<MaterialIdentifier, String>()
                    {
                        @Override
                        public String transform(MaterialIdentifier arg0)
                        {
                            return arg0.getCode();
                        }
                    }).toArray(new String[] {});

        // find by code
        Long2ObjectMap<Material> materialMap = asMaterials(getIteratorByCodes(materialCodes), null);

        // filter those elements which should not have been found (the same codes from different
        // types)
        filterCodesAndTypes(materialMap, identifiers);

        // at the moment (2012-03-13) it seems that in production databases the material code will
        // be unique. So there is no risk, that we will fetch a lot of items, just to filter them

        return materialMap;
    }

    private List<Material> convertAndEnrich(Long2ObjectMap<Material> materialMap,
            boolean withProperties)
    {
        if (withProperties)
        {
            enrichWithProperties(materialMap);
        }

        if (userId != null)
        {
            enrichWithMetaProjects(materialMap);
        }

        return asList(materialMap);
    }

    private DataIterator<MaterialRecord> getIteratorByType(MaterialType materialType)
    {
        assert materialType != null;
        return query.getMaterialsForMaterialType(materialType.getId());
    }

    private DataIterator<MaterialRecord> getIteratorByIds(Collection<Long> materialIds)
    {
        return query.getMaterialsForMaterialTypeWithIds(new LongOpenHashSet(
                materialIds));
    }

    private DataIterator<MaterialRecord> getIteratorByCodes(String[] materialCodes)
    {
        return query.getMaterialsForMaterialCodes(materialCodes);
    }

    private DataIterator<MaterialRecord> getIteratorByMetaprojectId(Long metaprojectId)
    {
        return query.getMaterialsForMetaprojectId(metaprojectId);
    }

    //
    // Enriching
    //

    private Long2ObjectMap<Material> asMaterials(Iterable<MaterialRecord> materials,
            MaterialType materialTypeOrNull)
    {
        List<MaterialRecord> materialRecords = asList(materials);
        final Long2ObjectMap<Material> materialMap =
                createMaterials(materialRecords, materialTypeOrNull);
        return materialMap;
    }

    private Long2ObjectMap<Material> createMaterials(Iterable<MaterialRecord> records,
            MaterialType materialTypeOrNull)
    {
        Long2ObjectMap<Material> materials = new Long2ObjectOpenHashMap<Material>();
        Long2ObjectMap<MaterialType> materialTypesOrNull = null;
        if (materialTypeOrNull == null)
        {
            materialTypesOrNull = getMaterialTypes();
        }
        for (MaterialRecord record : records)
        {
            materials.put(record.id,
                    createMaterial(record, materialTypeOrNull, materialTypesOrNull));
        }
        return materials;
    }

    private Material createMaterial(MaterialRecord record, MaterialType materialTypeOrNull,
            Long2ObjectMap<MaterialType> materialTypesOrNull)
    {
        Material material = new Material();
        material.setId(record.id);
        material.setCode(record.code);

        MaterialType materialType =
                materialTypeOrNull != null ? materialTypeOrNull : materialTypesOrNull
                        .get(record.maty_id);
        material.setMaterialType(materialType);
        material.setDatabaseInstance(databaseInstance);

        material.setRegistrator(getOrCreateActor(record.pers_id_registerer));
        material.setRegistrationDate(record.registration_timestamp);
        material.setModificationDate(record.modification_timestamp);

        material.setProperties(new ArrayList<IEntityProperty>());

        return material;
    }

    private static <T> List<T> asList(Iterable<T> items)
    {
        List<T> result = new ArrayList<T>();
        for (T item : items)
        {
            result.add(item);
        }
        return result;
    }

    private static <T> List<T> asList(Long2ObjectMap<T> items)
    {
        List<T> result = new ArrayList<T>();
        result.addAll(items.values());
        return result;
    }

    private void enrichWithMetaProjects(final Long2ObjectMap<Material> resultMap)
    {
        for (MetaProjectWithEntityId metaProject : query
                .getMetaprojects(resultMap.keySet(), userId))
        {
            Metaproject mp = new Metaproject();
            mp.setId(metaProject.id);
            mp.setCreationDate(metaProject.creation_date);
            mp.setDescription(metaProject.description);
            mp.setIdentifier("/" + metaProject.owner_name + "/" + metaProject.name);
            mp.setName(metaProject.name);
            mp.setOwnerId(metaProject.owner_name);
            mp.setPrivate(metaProject.is_private);

            Material material = resultMap.get(metaProject.entity_id);

            if (material != null)
            {
                Collection<Metaproject> mps = material.getMetaprojects();
                if (mps == null)
                {
                    mps = new HashSet<Metaproject>();
                    material.setMetaprojects(mps);
                }
                mps.add(mp);
            }
        }

    }

    private void enrichWithProperties(final Long2ObjectMap<Material> resultMap)
    {
        propertiesEnricher.enrich(resultMap.keySet(), new IEntityPropertiesHolderResolver()
            {
                @Override
                public Material get(long id)
                {
                    return resultMap.get(id);
                }
            });
    }

    private static Long2ObjectMap<Material> asMap(Iterable<Material> materials)
    {
        Long2ObjectMap<Material> map = new Long2ObjectOpenHashMap<Material>();
        for (Material material : materials)
        {
            map.put(material.getId(), material);
        }
        return map;
    }

    @Override
    public void enrichWithProperties(List<Material> materials)
    {
        setEmptyProperties(materials);
        Long2ObjectMap<Material> materialMap = asMap(materials);
        enrichWithProperties(materialMap);
        enrichRepeatedMaterialsWithProperties(materials, materialMap);
    }

    private void enrichRepeatedMaterialsWithProperties(List<Material> allMaterials,
            Long2ObjectMap<Material> enrichedMaterials)
    {
        for (Material material : allMaterials)
        {
            if (material.getProperties().isEmpty())
            {
                Material enrichedMaterial = enrichedMaterials.get(material.getId());
                material.setProperties(new ArrayList<IEntityProperty>(enrichedMaterial
                        .getProperties()));
            }
        }
    }

    private void setEmptyProperties(List<Material> materials)
    {
        for (Material material : materials)
        {
            material.setProperties(new ArrayList<IEntityProperty>());
        }
    }

    private Long2ObjectMap<MaterialType> getMaterialTypes()
    {
        final CodeRecord[] typeCodes = query.getMaterialTypes();
        final Long2ObjectOpenHashMap<MaterialType> materialTypeMap =
                new Long2ObjectOpenHashMap<MaterialType>(typeCodes.length);
        for (CodeRecord t : typeCodes)
        {
            final MaterialType type = new MaterialType();
            type.setCode(t.code);
            materialTypeMap.put(t.id, type);
        }
        materialTypeMap.trim();
        return materialTypeMap;
    }

}
