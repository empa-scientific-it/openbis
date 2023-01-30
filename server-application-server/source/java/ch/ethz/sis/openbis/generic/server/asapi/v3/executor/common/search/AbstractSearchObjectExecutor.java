/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Resource;

import org.apache.commons.collections4.map.ReferenceIdentityMap;
import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.TechIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.AbstractEntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.ModifierSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.RegistratorSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IMapDataSetByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.ISearchDataSetTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.ISearchExperimentTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IMapMaterialByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.ISearchMaterialTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person.ISearchPersonExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.IMapProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IMapSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ISearchSampleTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IMapTagByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.ISearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.ObjectAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchCriteriaTranslationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchCriteriaTranslatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchTranslationContext;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchObjectExecutor<CRITERIA extends AbstractObjectSearchCriteria<?>, OBJECT> implements
        ISearchObjectExecutor<CRITERIA, OBJECT>
{

    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IMapMaterialByIdExecutor mapMaterialByIdExecutor;

    @Autowired
    private IMapTagByIdExecutor mapTagByIdExecutor;

    @Autowired
    private ISearchMaterialTypeExecutor searchMaterialTypeExecutor;

    @Autowired
    private ISearchExperimentTypeExecutor searchExperimentTypeExecutor;

    @Autowired
    private ISearchSampleTypeExecutor searchSampleTypeExecutor;

    @Autowired
    private ISearchDataSetTypeExecutor searchDataSetTypeExecutor;

    @Autowired
    private ISearchPersonExecutor searchPersonExecutor;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    protected ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    protected IDAOFactory daoFactory;

    protected abstract List<OBJECT> doSearch(IOperationContext context, DetailedSearchCriteria criteria);

    @Override
    public List<OBJECT> search(IOperationContext context, CRITERIA criteria)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (criteria == null)
        {
            throw new IllegalArgumentException("Criteria cannot be null");
        }

        ISearchCriteria replacedCriteria = replaceCriteria(context, criteria);

        ISearchCriteriaTranslator translator =
                new SearchCriteriaTranslatorFactory(daoFactory, new ObjectAttributeProviderFactory()).getTranslator(replacedCriteria);

        SearchCriteriaTranslationResult translationResult =
                translator.translate(new SearchTranslationContext(context.getSession()), replacedCriteria);

        return doSearch(context, translationResult.getCriteria());
    }

    private ISearchCriteria replaceCriteria(IOperationContext context, AbstractCompositeSearchCriteria criteria)
    {
        List<ICriteriaReplacer> replacers = new LinkedList<ICriteriaReplacer>();
        replacers.add(new SpaceIdCriteriaReplacer());
        replacers.add(new ProjectIdCriteriaReplacer());
        replacers.add(new ExperimentIdCriteriaReplacer());
        replacers.add(new ExperimentTypeCriteriaReplacer());
        replacers.add(new SampleIdCriteriaReplacer());
        replacers.add(new SampleTypeCriteriaReplacer());
        replacers.add(new DataSetIdCriteriaReplacer());
        replacers.add(new DataSetTypeCriteriaReplacer());
        replacers.add(new MaterialIdCriteriaReplacer());
        replacers.add(new MaterialPermIdCriteriaReplacer());
        replacers.add(new MaterialTypeCriteriaReplacer());
        replacers.add(new RegistratorCriteriaReplacer());
        replacers.add(new ModifierCriteriaReplacer());
        replacers.add(new TagIdCriteriaReplacer());

        Map<ICriteriaReplacer, Set<ISearchCriteria>> toReplaceMap = new HashMap<ICriteriaReplacer, Set<ISearchCriteria>>();
        collectCriteriaToReplace(context, new Stack<ISearchCriteria>(), criteria, replacers, toReplaceMap);

        if (false == toReplaceMap.isEmpty())
        {
            Map<ISearchCriteria, ISearchCriteria> replacementMap = createCriteriaReplacements(context, toReplaceMap);
            return replaceCriteria(criteria, replacementMap);
        } else
        {
            return criteria;
        }
    }

    private void collectCriteriaToReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria,
            ISearchCriteria criteria, List<ICriteriaReplacer> replacers, Map<ICriteriaReplacer, Set<ISearchCriteria>> toReplaceMap)
    {
        for (ICriteriaReplacer replacer : replacers)
        {
            if (replacer.canReplace(context, parentCriteria, criteria))
            {
                Set<ISearchCriteria> toReplace = toReplaceMap.get(replacer);
                if (toReplace == null)
                {
                    toReplace = new HashSet<ISearchCriteria>();
                    toReplaceMap.put(replacer, toReplace);
                }
                toReplace.add(criteria);
                return;
            }
        }

        if (criteria instanceof AbstractCompositeSearchCriteria)
        {
            parentCriteria.push(criteria);

            AbstractCompositeSearchCriteria compositeCriteria = (AbstractCompositeSearchCriteria) criteria;

            for (ISearchCriteria subCriterion : compositeCriteria.getCriteria())
            {
                collectCriteriaToReplace(context, parentCriteria, subCriterion, replacers, toReplaceMap);
            }

            parentCriteria.pop();
        }
    }

    private Map<ISearchCriteria, ISearchCriteria> createCriteriaReplacements(IOperationContext context,
            Map<ICriteriaReplacer, Set<ISearchCriteria>> toReplaceMap)
    {
        Map<ISearchCriteria, ISearchCriteria> result = new HashMap<ISearchCriteria, ISearchCriteria>();

        for (ICriteriaReplacer replacer : toReplaceMap.keySet())
        {
            Set<ISearchCriteria> toReplace = toReplaceMap.get(replacer);
            Map<ISearchCriteria, ISearchCriteria> replaced = replacer.replace(context, toReplace);
            result.putAll(replaced);
        }

        return result;
    }

    private ISearchCriteria replaceCriteria(ISearchCriteria criteria, Map<ISearchCriteria, ISearchCriteria> replacementMap)
    {
        if (replacementMap.containsKey(criteria))
        {
            return replacementMap.get(criteria);
        }

        if (criteria instanceof AbstractCompositeSearchCriteria)
        {
            AbstractCompositeSearchCriteria compositeCriteria = (AbstractCompositeSearchCriteria) criteria;
            List<ISearchCriteria> newSubCriteria = new LinkedList<ISearchCriteria>();

            for (ISearchCriteria subCriterion : compositeCriteria.getCriteria())
            {
                ISearchCriteria newSubCriterion = replaceCriteria(subCriterion, replacementMap);
                if (newSubCriterion != null)
                {
                    newSubCriteria.add(newSubCriterion);
                }
            }

            compositeCriteria.setCriteria(newSubCriteria);
        }

        return criteria;
    }

    private interface ICriteriaReplacer
    {

        public boolean canReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria, ISearchCriteria criteria);

        public Map<ISearchCriteria, ISearchCriteria> replace(IOperationContext context, Collection<ISearchCriteria> criteria);

    }

    @SuppressWarnings("hiding")
    private abstract class AbstractIdCriteriaReplacer<ID extends IObjectId, OBJECT> implements ICriteriaReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria, ISearchCriteria criteria)
        {
            if (criteria instanceof IdSearchCriteria<?>)
            {
                IObjectId id = ((IdSearchCriteria<?>) criteria).getId();
                return getIdClass().isAssignableFrom(id.getClass());
            } else
            {
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<ISearchCriteria, ISearchCriteria> replace(IOperationContext context, Collection<ISearchCriteria> criteria)
        {
            Set<ID> objectIds = new HashSet<ID>();

            for (ISearchCriteria criterion : criteria)
            {
                IdSearchCriteria<?> idCriterion = (IdSearchCriteria<?>) criterion;
                objectIds.add((ID) idCriterion.getId());
            }

            Map<ISearchCriteria, ISearchCriteria> criterionMap = new HashMap<ISearchCriteria, ISearchCriteria>();
            Map<ID, OBJECT> objectMap = getObjectMap(context, objectIds);

            for (ISearchCriteria criterion : criteria)
            {
                IdSearchCriteria<?> idCriterion = (IdSearchCriteria<?>) criterion;
                OBJECT object = objectMap.get(idCriterion.getId());
                criterionMap.put(criterion, createReplacement(context, idCriterion, object));
            }

            return criterionMap;
        }

        protected abstract Class<ID> getIdClass();

        protected abstract Map<ID, OBJECT> getObjectMap(IOperationContext context, Collection<ID> objectIds);

        protected abstract ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, OBJECT object);

    }

    private class SpaceIdCriteriaReplacer extends AbstractIdCriteriaReplacer<ISpaceId, SpacePE>
    {

        @Override
        protected Class<ISpaceId> getIdClass()
        {
            return ISpaceId.class;
        }

        @Override
        protected Map<ISpaceId, SpacePE> getObjectMap(IOperationContext context, Collection<ISpaceId> spaceIds)
        {
            return mapSpaceByIdExecutor.map(context, spaceIds);
        }

        @Override
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, SpacePE space)
        {
            CodeSearchCriteria replacement = new CodeSearchCriteria();
            if (space == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(space.getCode());
            }
            return replacement;
        }

    }

    private class ProjectIdCriteriaReplacer extends AbstractIdCriteriaReplacer<IProjectId, ProjectPE>
    {

        @Override
        protected Class<IProjectId> getIdClass()
        {
            return IProjectId.class;
        }

        @Override
        protected Map<IProjectId, ProjectPE> getObjectMap(IOperationContext context, Collection<IProjectId> projectIds)
        {
            return mapProjectByIdExecutor.map(context, projectIds);
        }

        @Override
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, ProjectPE project)
        {
            PermIdSearchCriteria replacement = new PermIdSearchCriteria();
            if (project == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(project.getPermId());
            }
            return replacement;
        }

    }

    private class ExperimentIdCriteriaReplacer extends AbstractIdCriteriaReplacer<IExperimentId, ExperimentPE>
    {

        @Override
        protected Class<IExperimentId> getIdClass()
        {
            return IExperimentId.class;
        }

        @Override
        protected Map<IExperimentId, ExperimentPE> getObjectMap(IOperationContext context, Collection<IExperimentId> experimentIds)
        {
            return mapExperimentByIdExecutor.map(context, experimentIds);
        }

        @Override
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, ExperimentPE experiment)
        {
            PermIdSearchCriteria replacement = new PermIdSearchCriteria();
            if (experiment == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(experiment.getPermId());
            }
            return replacement;
        }

    }

    private class SampleIdCriteriaReplacer extends AbstractIdCriteriaReplacer<ISampleId, SamplePE>
    {

        @Override
        protected Class<ISampleId> getIdClass()
        {
            return ISampleId.class;
        }

        @Override
        protected Map<ISampleId, SamplePE> getObjectMap(IOperationContext context, Collection<ISampleId> sampleIds)
        {
            return mapSampleByIdExecutor.map(context, sampleIds);
        }

        @Override
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, SamplePE sample)
        {
            PermIdSearchCriteria replacement = new PermIdSearchCriteria();
            if (sample == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(sample.getPermId());
            }
            return replacement;
        }

    }

    private class DataSetIdCriteriaReplacer extends AbstractIdCriteriaReplacer<IDataSetId, DataPE>
    {

        @Override
        protected Class<IDataSetId> getIdClass()
        {
            return IDataSetId.class;
        }

        @Override
        protected Map<IDataSetId, DataPE> getObjectMap(IOperationContext context, Collection<IDataSetId> dataSetIds)
        {
            return mapDataSetByIdExecutor.map(context, dataSetIds);
        }

        @Override
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, DataPE dataSet)
        {
            PermIdSearchCriteria replacement = new PermIdSearchCriteria();
            if (dataSet == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(dataSet.getPermId());
            }
            return replacement;
        }

    }

    private class MaterialIdCriteriaReplacer extends AbstractIdCriteriaReplacer<IMaterialId, MaterialPE>
    {

        @Override
        protected Class<IMaterialId> getIdClass()
        {
            return IMaterialId.class;
        }

        @Override
        protected Map<IMaterialId, MaterialPE> getObjectMap(IOperationContext context, Collection<IMaterialId> materialIds)
        {
            return mapMaterialByIdExecutor.map(context, materialIds);
        }

        @Override
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, MaterialPE material)
        {
            TechIdSearchCriteria replacement = new TechIdSearchCriteria();
            if (material == null)
            {
                replacement.thatEquals(-1);
            } else
            {
                replacement.thatEquals(material.getId());
            }
            return replacement;
        }

    }

    private class MaterialPermIdCriteriaReplacer implements ICriteriaReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria, ISearchCriteria criteria)
        {
            return criteria instanceof PermIdSearchCriteria && parentCriteria.peek() instanceof MaterialSearchCriteria;
        }

        @Override
        public Map<ISearchCriteria, ISearchCriteria> replace(IOperationContext context, Collection<ISearchCriteria> criteria)
        {
            throw new UnsupportedOperationException("Please use criteria.withId().thatEquals(new MaterialPermId('CODE','TYPE')) instead.");
        }

    }

    private class TagIdCriteriaReplacer extends AbstractIdCriteriaReplacer<ITagId, MetaprojectPE>
    {

        @Override
        protected Class<ITagId> getIdClass()
        {
            return ITagId.class;
        }

        @Override
        protected Map<ITagId, MetaprojectPE> getObjectMap(IOperationContext context, Collection<ITagId> tagIds)
        {
            return mapTagByIdExecutor.map(context, tagIds);
        }

        @Override
        protected ISearchCriteria createReplacement(IOperationContext context, ISearchCriteria criteria, MetaprojectPE tag)
        {
            CodeSearchCriteria replacement = new CodeSearchCriteria();
            if (tag == null)
            {
                replacement.thatEquals("#");
            } else
            {
                replacement.thatEquals(tag.getName());
            }
            return replacement;
        }

    }

    private abstract class AbstractEntityTypeCriteriaReplacer implements ICriteriaReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria, ISearchCriteria criteria)
        {
            if (criteria instanceof AbstractEntityTypeSearchCriteria)
            {
                return getEntityTypeCriteriaClass().isAssignableFrom(criteria.getClass());
            } else
            {
                return false;
            }
        }

        @Override
        public Map<ISearchCriteria, ISearchCriteria> replace(IOperationContext context, Collection<ISearchCriteria> criteria)
        {
            Map<ISearchCriteria, ISearchCriteria> replacementMap = new ReferenceIdentityMap<ISearchCriteria, ISearchCriteria>();

            for (ISearchCriteria criterion : criteria)
            {
                AbstractEntityTypeSearchCriteria entityTypeCriterion = (AbstractEntityTypeSearchCriteria) criterion;
                List<? extends EntityTypePE> entityTypes = searchEntityTypes(context, entityTypeCriterion);
                List<String> entityTypeCodes = new ArrayList<String>();

                if (entityTypes == null || entityTypes.isEmpty())
                {
                    entityTypeCodes.add("#");
                } else
                {
                    for (EntityTypePE entityType : entityTypes)
                    {
                        entityTypeCodes.add(entityType.getCode());
                    }
                }

                replacementMap.put(entityTypeCriterion, createNewEntityTypeCriteria(entityTypeCodes));
            }

            return replacementMap;
        }

        protected abstract Class<? extends AbstractEntityTypeSearchCriteria> getEntityTypeCriteriaClass();

        protected abstract List<? extends EntityTypePE> searchEntityTypes(IOperationContext context, AbstractEntityTypeSearchCriteria criteria);

        protected abstract AbstractEntityTypeSearchCriteria createNewEntityTypeCriteria(List<String> entityTypeCodes);

    }

    private class ExperimentTypeCriteriaReplacer extends AbstractEntityTypeCriteriaReplacer
    {

        @Override
        protected Class<? extends AbstractEntityTypeSearchCriteria> getEntityTypeCriteriaClass()
        {
            return ExperimentTypeSearchCriteria.class;
        }

        @Override
        protected List<? extends EntityTypePE> searchEntityTypes(IOperationContext context, AbstractEntityTypeSearchCriteria criteria)
        {
            return searchExperimentTypeExecutor.search(context, (ExperimentTypeSearchCriteria) criteria);
        }

        @Override
        protected AbstractEntityTypeSearchCriteria createNewEntityTypeCriteria(List<String> entityTypeCodes)
        {
            ExperimentTypeSearchCriteria criteria = new ExperimentTypeSearchCriteria();
            criteria.withCodes().thatIn(entityTypeCodes);
            return criteria;
        }

    }

    private class SampleTypeCriteriaReplacer extends AbstractEntityTypeCriteriaReplacer
    {

        @Override
        protected Class<? extends AbstractEntityTypeSearchCriteria> getEntityTypeCriteriaClass()
        {
            return SampleTypeSearchCriteria.class;
        }

        @Override
        protected List<? extends EntityTypePE> searchEntityTypes(IOperationContext context, AbstractEntityTypeSearchCriteria criteria)
        {
            return searchSampleTypeExecutor.search(context, (SampleTypeSearchCriteria) criteria);
        }

        @Override
        protected AbstractEntityTypeSearchCriteria createNewEntityTypeCriteria(List<String> entityTypeCodes)
        {
            SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
            criteria.withCodes().thatIn(entityTypeCodes);
            return criteria;
        }

    }

    private class DataSetTypeCriteriaReplacer extends AbstractEntityTypeCriteriaReplacer
    {

        @Override
        protected Class<? extends AbstractEntityTypeSearchCriteria> getEntityTypeCriteriaClass()
        {
            return DataSetTypeSearchCriteria.class;
        }

        @Override
        protected List<? extends EntityTypePE> searchEntityTypes(IOperationContext context, AbstractEntityTypeSearchCriteria criteria)
        {
            return searchDataSetTypeExecutor.search(context, (DataSetTypeSearchCriteria) criteria);
        }

        @Override
        protected AbstractEntityTypeSearchCriteria createNewEntityTypeCriteria(List<String> entityTypeCodes)
        {
            DataSetTypeSearchCriteria criteria = new DataSetTypeSearchCriteria();
            criteria.withCodes().thatIn(entityTypeCodes);
            return criteria;
        }

    }

    private class MaterialTypeCriteriaReplacer extends AbstractEntityTypeCriteriaReplacer
    {

        @Override
        protected Class<? extends AbstractEntityTypeSearchCriteria> getEntityTypeCriteriaClass()
        {
            return MaterialTypeSearchCriteria.class;
        }

        @Override
        protected List<? extends EntityTypePE> searchEntityTypes(IOperationContext context, AbstractEntityTypeSearchCriteria criteria)
        {
            return searchMaterialTypeExecutor.search(context, (MaterialTypeSearchCriteria) criteria);
        }

        @Override
        protected AbstractEntityTypeSearchCriteria createNewEntityTypeCriteria(List<String> entityTypeCodes)
        {
            MaterialTypeSearchCriteria criteria = new MaterialTypeSearchCriteria();
            criteria.withCodes().thatIn(entityTypeCodes);
            return criteria;
        }

    }

    private abstract class AbstractPersonCriteriaReplacer implements ICriteriaReplacer
    {

        @Override
        public boolean canReplace(IOperationContext context, Stack<ISearchCriteria> parentCriteria, ISearchCriteria criteria)
        {
            if (criteria instanceof PersonSearchCriteria)
            {
                return getPersonCriteriaClass().isAssignableFrom(criteria.getClass());
            } else
            {
                return false;
            }
        }

        @Override
        public Map<ISearchCriteria, ISearchCriteria> replace(IOperationContext context, Collection<ISearchCriteria> criteria)
        {
            Map<ISearchCriteria, ISearchCriteria> replacementMap = new ReferenceIdentityMap<ISearchCriteria, ISearchCriteria>();

            for (ISearchCriteria criterion : criteria)
            {
                PersonSearchCriteria personCriterion = (PersonSearchCriteria) criterion;
                List<? extends PersonPE> persons = searchPersons(context, personCriterion);
                List<String> personUserIds = new ArrayList<String>();

                if (persons == null || persons.isEmpty())
                {
                    personUserIds.add("#");
                } else
                {
                    for (PersonPE person : persons)
                    {
                        personUserIds.add(person.getUserId());
                    }
                }

                replacementMap.put(personCriterion, createNewPersonCriteria(personUserIds));
            }

            return replacementMap;
        }

        protected abstract Class<? extends PersonSearchCriteria> getPersonCriteriaClass();

        protected abstract List<? extends PersonPE> searchPersons(IOperationContext context, PersonSearchCriteria criteria);

        protected abstract PersonSearchCriteria createNewPersonCriteria(List<String> personUserIds);

    }

    private class RegistratorCriteriaReplacer extends AbstractPersonCriteriaReplacer
    {

        @Override
        protected Class<? extends PersonSearchCriteria> getPersonCriteriaClass()
        {
            return RegistratorSearchCriteria.class;
        }

        @Override
        protected List<? extends PersonPE> searchPersons(IOperationContext context, PersonSearchCriteria criteria)
        {
            return searchPersonExecutor.search(context, criteria);
        }

        @Override
        protected PersonSearchCriteria createNewPersonCriteria(List<String> personUserIds)
        {
            RegistratorSearchCriteria criteria = new RegistratorSearchCriteria();
            criteria.withUserIds().thatIn(personUserIds);
            return criteria;
        }

    }

    private class ModifierCriteriaReplacer extends AbstractPersonCriteriaReplacer
    {

        @Override
        protected Class<? extends PersonSearchCriteria> getPersonCriteriaClass()
        {
            return ModifierSearchCriteria.class;
        }

        @Override
        protected List<? extends PersonPE> searchPersons(IOperationContext context, PersonSearchCriteria criteria)
        {
            return searchPersonExecutor.search(context, criteria);
        }

        @Override
        protected PersonSearchCriteria createNewPersonCriteria(List<String> personUserIds)
        {
            ModifierSearchCriteria criteria = new ModifierSearchCriteria();
            criteria.withUserIds().thatIn(personUserIds);
            return criteria;
        }

    }

}
