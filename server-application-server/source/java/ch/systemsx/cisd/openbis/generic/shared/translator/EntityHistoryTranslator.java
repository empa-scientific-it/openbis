/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractEntityHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractEntityPropertyHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IRelatedEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.IRelatedEntityFinder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MatchingContentCopy;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelatedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelatedExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelatedExternalDms;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelatedProject;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelatedSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelatedSpace;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author Franz-Josef Elmer
 */
public class EntityHistoryTranslator
{

    private static Map<Class<? extends IRelatedEntity>, IRelatedEntityTranslator> RELATED_TRANSLATORS =
            new HashMap<Class<? extends IRelatedEntity>, IRelatedEntityTranslator>();

    static
    {
        RELATED_TRANSLATORS.put(RelatedSpace.class, new RelatedSpaceTranslator());
        RELATED_TRANSLATORS.put(RelatedProject.class, new RelatedProjectTranslator());
        RELATED_TRANSLATORS.put(RelatedExperiment.class, new RelatedExperimentTranslator());
        RELATED_TRANSLATORS.put(RelatedSample.class, new RelatedSampleTranslator());
        RELATED_TRANSLATORS.put(RelatedDataSet.class, new RelatedDataSetTranslator());
        RELATED_TRANSLATORS.put(RelatedExternalDms.class, new RelatedExternalDmsTranslator());
    }

    public static List<EntityHistory> translate(List<AbstractEntityPropertyHistoryPE> history,
            String baseIndexURL, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator, IRelatedEntityFinder finder)
    {
        List<EntityHistory> result = new ArrayList<EntityHistory>();
        HashMap<PropertyTypePE, PropertyType> cache = new HashMap<PropertyTypePE, PropertyType>();
        HashMap<MaterialTypePE, MaterialType> materialTypesCache = new HashMap<MaterialTypePE, MaterialType>();
        for (AbstractEntityPropertyHistoryPE entityPropertyHistory : history)
        {
            result.add(translate(entityPropertyHistory, materialTypesCache, cache, baseIndexURL,
                    managedPropertyEvaluatorFactory, samplePropertyAccessValidator, finder));
        }
        return result;
    }

    private static EntityHistory translate(AbstractEntityPropertyHistoryPE entityPropertyHistory,
            Map<MaterialTypePE, MaterialType> materialTypeCache,
            Map<PropertyTypePE, PropertyType> cache, String baseIndexURL,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator, IRelatedEntityFinder finder)
    {
        EntityHistory result = new EntityHistory();
        result.setAuthor(PersonTranslator.translate(entityPropertyHistory.getAuthor()));
        result.setValidFromDate(entityPropertyHistory.getValidFromDate());
        result.setValidUntilDate(entityPropertyHistory.getValidUntilDate());
        result.setValue(entityPropertyHistory.getValue());
        result.setMaterial(entityPropertyHistory.getMaterial());
        result.setVocabularyTerm(entityPropertyHistory.getVocabularyTerm());
        if (entityPropertyHistory.getEntityTypePropertyType() != null)
        {
            result.setPropertyType(PropertyTypeTranslator.translate(entityPropertyHistory
                    .getEntityTypePropertyType().getPropertyType(), materialTypeCache, cache));
        }

        if (entityPropertyHistory instanceof AbstractEntityHistoryPE)
        {
            AbstractEntityHistoryPE entityHistory = (AbstractEntityHistoryPE) entityPropertyHistory;
            result.setRelatedEntityPermId(entityHistory.getEntityPermId());

            if (entityHistory.getRelationType() != null)
            {
                result.setRelationType(entityHistory.getRelationType().getDescription(null));
            }

            if (entityHistory.getRelatedEntity() != null)
            {
                IRelatedEntityTranslator relatedTranslator = RELATED_TRANSLATORS.get(entityHistory.getRelatedEntity().getClass());

                if (relatedTranslator == null)
                {
                    throw new RuntimeException("Unknown related entity: " + entityHistory.getRelatedEntity().getClass());
                }

                relatedTranslator.translate(entityHistory, result, finder, managedPropertyEvaluatorFactory,
                        samplePropertyAccessValidator, baseIndexURL);
            }
        }

        return result;
    }

    private static interface IRelatedEntityTranslator
    {

        public void translate(AbstractEntityHistoryPE historyPE, EntityHistory history, IRelatedEntityFinder finder,
                IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
                IValidator<IIdentifierHolder> samplePropertyAccessValidator, String baseIndexURL);

    }

    private static class RelatedSpaceTranslator implements IRelatedEntityTranslator
    {

        @Override
        public void translate(AbstractEntityHistoryPE historyPE, EntityHistory history, IRelatedEntityFinder finder,
                IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
                IValidator<IIdentifierHolder> samplePropertyAccessValidator, String baseIndexURL)
        {
            RelatedSpace related = (RelatedSpace) historyPE.getRelatedEntity();
            SpacePE relatedPE = finder.findById(SpacePE.class, related.getEntityId());

            if (relatedPE != null)
            {
                history.setRelatedSpace(SpaceTranslator.translate(relatedPE));
            }

            if (historyPE.getRelationType() != null)
            {
                history.setRelationType(historyPE.getRelationType().getDescription("Space"));
            }
        }

    }

    private static class RelatedProjectTranslator implements IRelatedEntityTranslator
    {

        @Override
        public void translate(AbstractEntityHistoryPE historyPE, EntityHistory history, IRelatedEntityFinder finder,
                IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
                IValidator<IIdentifierHolder> samplePropertyAccessValidator, String baseIndexURL)
        {
            RelatedProject related = (RelatedProject) historyPE.getRelatedEntity();
            ProjectPE relatedPE = finder.findById(ProjectPE.class, related.getEntityId());

            if (relatedPE != null)
            {
                history.setRelatedProject(ProjectTranslator.translate(relatedPE));
            }

            if (historyPE.getRelationType() != null)
            {
                history.setRelationType(historyPE.getRelationType().getDescription("Project"));
            }
        }

    }

    private static class RelatedExperimentTranslator implements IRelatedEntityTranslator
    {

        @Override
        public void translate(AbstractEntityHistoryPE historyPE, EntityHistory history, IRelatedEntityFinder finder,
                IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
                IValidator<IIdentifierHolder> samplePropertyAccessValidator, String baseIndexURL)
        {
            RelatedExperiment related = (RelatedExperiment) historyPE.getRelatedEntity();
            ExperimentPE relatedPE = finder.findById(ExperimentPE.class, related.getEntityId());

            if (relatedPE != null)
            {
                history.setRelatedEntity(ExperimentTranslator.translate(relatedPE, baseIndexURL, null,
                        managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
            }

            if (historyPE.getRelationType() != null)
            {
                history.setRelationType(historyPE.getRelationType().getDescription(EntityKind.EXPERIMENT.getDescription()));
            }
        }

    }

    private static class RelatedSampleTranslator implements IRelatedEntityTranslator
    {

        @Override
        public void translate(AbstractEntityHistoryPE historyPE, EntityHistory history, IRelatedEntityFinder finder,
                IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
                IValidator<IIdentifierHolder> samplePropertyAccessValidator, String baseIndexURL)
        {
            RelatedSample related = (RelatedSample) historyPE.getRelatedEntity();
            SamplePE relatedPE = finder.findById(SamplePE.class, related.getEntityId());

            if (relatedPE != null)
            {
                history.setRelatedEntity(SampleTranslator.translate(relatedPE, baseIndexURL, null,
                        managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
            }

            if (historyPE.getRelationType() != null)
            {
                history.setRelationType(historyPE.getRelationType().getDescription(EntityKind.SAMPLE.getDescription()));
            }
        }

    }

    private static class RelatedDataSetTranslator implements IRelatedEntityTranslator
    {

        @Override
        public void translate(AbstractEntityHistoryPE historyPE, EntityHistory history, IRelatedEntityFinder finder,
                IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
                IValidator<IIdentifierHolder> samplePropertyAccessValidator, String baseIndexURL)
        {
            RelatedDataSet related = (RelatedDataSet) historyPE.getRelatedEntity();
            DataPE relatedPE = finder.findById(DataPE.class, related.getEntityId());

            if (relatedPE != null)
            {
                AbstractExternalData data = DataSetTranslator.translateBasicProperties(relatedPE);
                data.setExperiment(ExperimentTranslator.translate(relatedPE.getExperiment(), baseIndexURL, null,
                        managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
                data.setSample(SampleTranslator.translate(relatedPE.tryGetSample(), baseIndexURL, null,
                        managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
                history.setRelatedEntity(data);
            }

            if (historyPE.getRelationType() != null)
            {
                history.setRelationType(historyPE.getRelationType().getDescription(EntityKind.DATA_SET.getDescription()));
            }
        }

    }

    private static class RelatedExternalDmsTranslator implements IRelatedEntityTranslator
    {

        @Override
        public void translate(AbstractEntityHistoryPE historyPE, EntityHistory history, IRelatedEntityFinder finder,
                IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
                IValidator<IIdentifierHolder> samplePropertyAccessValidator, String baseIndexURL)
        {
            RelatedExternalDms related = (RelatedExternalDms) historyPE.getRelatedEntity();
            ExternalDataManagementSystemPE relatedPE =
                    finder.findById(ExternalDataManagementSystemPE.class, related.getEntityId());

            if (relatedPE != null)
            {
                DataSetHistoryPE dsHistoryPE = (DataSetHistoryPE) historyPE;

                MatchingContentCopy contentCopy = new MatchingContentCopy(dsHistoryPE.getExternalCode(), dsHistoryPE.getPath(),
                        dsHistoryPE.getGitCommitHash(), dsHistoryPE.getGitRepositoryId(), dsHistoryPE.getExternalDmsCode(), 
                        dsHistoryPE.getExternalDmsLabel(), dsHistoryPE.getExternalDmsAddress(), relatedPE);

                history.setValue(contentCopy.toString());
                PropertyType pt = new PropertyType();
                pt.setCode("Content copy");
                pt.setLabel("Content copy");
                pt.setDataType(new DataType(DataTypeCode.VARCHAR));
                history.setPropertyType(pt);
            }
        }

    }

}
