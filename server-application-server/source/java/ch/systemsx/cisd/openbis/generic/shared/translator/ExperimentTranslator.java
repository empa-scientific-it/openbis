/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Experiment} &lt;---&gt; {@link ExperimentPE} translator.
 * 
 * @author Tomasz Pylak
 */
public final class ExperimentTranslator
{

    public enum LoadableFields
    {
        ATTACHMENTS, PROPERTIES
    }

    private ExperimentTranslator()
    {
        // Can not be instantiated.
    }

    private static void setProperties(final ExperimentPE experiment, final Experiment result,
            final boolean rawManagedProperties,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        if (experiment.isPropertiesInitialized())
        {
            HashMap<MaterialTypePE, MaterialType> materialTypeCache = new HashMap<MaterialTypePE, MaterialType>();
            HashMap<PropertyTypePE, PropertyType> cache = new HashMap<PropertyTypePE, PropertyType>();
            if (rawManagedProperties)
            {
                result.setProperties(EntityPropertyTranslator.translateRaw(
                        experiment.getProperties(), materialTypeCache, cache,
                        managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
            } else
            {
                result.setProperties(EntityPropertyTranslator.translate(experiment.getProperties(),
                        materialTypeCache, cache,
                        managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
            }
        } else
        {
            result.setProperties(new ArrayList<IEntityProperty>());
        }
    }

    public final static Experiment translateWithoutRevealingData(final ExperimentPE experiment,
            Collection<Metaproject> metaprojects)
    {
        final Experiment result = new Experiment(true);

        result.setId(HibernateUtils.getId(experiment));
        result.setPermId(experiment.getPermId());
        result.setProperties(new ArrayList<IEntityProperty>());
        result.setMetaprojects(metaprojects);

        return result;
    }

    public final static Experiment translateWithoutRevealingData(final Experiment experiment)
    {
        final Experiment result = new Experiment(true);

        result.setId(HibernateUtils.getId(experiment));
        result.setPermId(experiment.getPermId());
        result.setProperties(new ArrayList<IEntityProperty>());
        result.setMetaprojects(experiment.getMetaprojects());

        return result;
    }

    public final static Experiment translate(final ExperimentPE experiment, String baseIndexURL,
            Collection<Metaproject> metaprojects,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator,
            final LoadableFields... withFields)
    {
        return translate(experiment, baseIndexURL, false, metaprojects,
                managedPropertyEvaluatorFactory, samplePropertyAccessValidator, withFields);
    }

    public final static Experiment translate(final ExperimentPE experiment, String baseIndexURL,
            final boolean rawManagedProperties, Collection<Metaproject> metaprojects,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator,
            final LoadableFields... withFields)
    {
        if (experiment == null)
        {
            return null;
        }
        final Experiment result = new Experiment();
        result.setId(HibernateUtils.getId(experiment));
        result.setModificationDate(experiment.getModificationDate());
        result.setCode(experiment.getCode());
        result.setPermId(experiment.getPermId());
        result.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.EXPERIMENT,
                experiment.getPermId()));
        result.setExperimentType(translate(experiment.getExperimentType(),
                new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>()));
        result.setIdentifier(experiment.getIdentifier());
        result.setProject(ProjectTranslator.translate(experiment.getProject()));
        result.setRegistrationDate(experiment.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(experiment.getRegistrator()));
        result.setModifier(PersonTranslator.translate(experiment.getModifier()));
        result.setDeletion(DeletionTranslator.translate(experiment.getDeletion()));
        result.setVersion(experiment.getVersion());
        result.setMetaData(experiment.getMetaData());
        for (final LoadableFields field : withFields)
        {
            switch (field)
            {
                case PROPERTIES:
                    setProperties(experiment, result, rawManagedProperties,
                            managedPropertyEvaluatorFactory, samplePropertyAccessValidator);
                    break;
                case ATTACHMENTS:
                    result.setAttachments(AttachmentTranslator.translate(
                            experiment.getAttachments(), baseIndexURL));
                    break;
                default:
                    break;
            }
        }

        if (metaprojects != null)
        {
            result.setMetaprojects(metaprojects);
        }

        return result;
    }

    // NOTE: when translating list of experiments managed properties will contain raw value
    public final static List<Experiment> translate(final List<ExperimentPE> experiments,
            String baseIndexURL, Map<Long, Set<Metaproject>> metaprojects,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        final List<Experiment> result = new ArrayList<Experiment>(experiments.size());
        for (final ExperimentPE experiment : experiments)
        {
            HibernateUtils.initialize(experiment.getProperties());
            result.add(ExperimentTranslator.translate(experiment, baseIndexURL, true,
                    metaprojects.get(experiment.getId()), managedPropertyEvaluatorFactory,
                    samplePropertyAccessValidator, LoadableFields.PROPERTIES));
        }
        return result;
    }

    public final static ExperimentType translate(final ExperimentTypePE experimentType,
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        final ExperimentType result = new ExperimentType();
        result.setCode(experimentType.getCode());
        result.setDescription(experimentType.getDescription());
        result.setModificationDate(experimentType.getModificationDate());
        result.setValidationScript(ScriptTranslator.translate(experimentType.getValidationScript()));

        result.setExperimentTypePropertyTypes(ExperimentTypePropertyTypeTranslator.translate(
                experimentType.getExperimentTypePropertyTypes(), result, materialTypeCache, cacheOrNull));

        return result;
    }

    public final static ExperimentTypePE translate(final ExperimentType experimentType)
    {
        final ExperimentTypePE result = new ExperimentTypePE();
        result.setCode(experimentType.getCode());
        result.setDescription(experimentType.getDescription());

        return result;
    }

    public final static List<ExperimentType> translate(final List<ExperimentTypePE> experimentTypes)
    {
        final List<ExperimentType> result = new ArrayList<ExperimentType>(experimentTypes.size());
        for (final ExperimentTypePE experimentType : experimentTypes)
        {
            result.add(translate(experimentType, new HashMap<MaterialTypePE, MaterialType>(), 
                    new HashMap<PropertyTypePE, PropertyType>()));
        }
        return result;
    }

}
