/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.DynamicPropertyPluginEvaluationOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.DynamicPropertyPluginEvaluationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.EntityValidationPluginEvaluationOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.EntityValidationPluginEvaluationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.PluginEvaluationOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.PluginEvaluationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IMapDataSetByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IMapMaterialByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IMapSampleByIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.DynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.INonAbstractEntityAdapter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.JythonEntityValidationCalculator.IValidationRequestDelegate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.api.IEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author pkupczyk
 */
@Component
public class EvaluatePluginExecutor implements IEvaluatePluginExecutor
{

    @Autowired
    private IPluginAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Autowired
    private IMapPluginByIdExecutor mapPluginByIdExecutor;

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IMapMaterialByIdExecutor mapMaterialByIdExecutor;

    @Autowired
    private IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory;

    @Autowired
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @Autowired
    private IEntityValidatorFactory entityValidationFactory;

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public PluginEvaluationResult execute(IOperationContext context, PluginEvaluationOptions options)
    {
        if (options == null)
        {
            throw new UserFailureException("Plugin evaluation options cannot be null");
        }
        if (options.getPluginId() == null && StringUtils.isBlank(options.getPluginScript()))
        {
            throw new UserFailureException("Plugin id and plugin script cannot be both null");
        }
        if (options.getPluginId() != null && false == StringUtils.isBlank(options.getPluginScript()))
        {
            throw new UserFailureException("Plugin id and plugin script cannot be both specified");
        }

        authorizationExecutor.canEvaluate(context);

        if (options.getPluginId() != null)
        {
            ScriptPE plugin = getPlugin(context, options.getPluginId());

            if (plugin != null)
            {
                return evaluatePluginFromDatabase(context, plugin, options);
            } else
            {
                throw new ObjectNotFoundException(options.getPluginId());
            }
        } else
        {
            return evaluatePluginFromScript(context, options);
        }
    }

    private PluginEvaluationResult evaluatePluginFromDatabase(IOperationContext context, ScriptPE plugin, PluginEvaluationOptions options)
    {
        switch (plugin.getScriptType())
        {
            case DYNAMIC_PROPERTY:
                if (options instanceof DynamicPropertyPluginEvaluationOptions)
                {
                    return evaluateDynamicProperty(context, plugin.getName(), plugin.getPluginType(), plugin.getEntityKind(), plugin.getScript(),
                            (DynamicPropertyPluginEvaluationOptions) options);
                } else
                {
                    throw UserFailureException.fromTemplate("'%s' is a dynamic property plugin. It requires '%s' evaluation options, but got '%s'",
                            options.getPluginId(), DynamicPropertyPluginEvaluationOptions.class.getSimpleName(),
                            options.getClass().getSimpleName());
                }
            case ENTITY_VALIDATION:
                if (options instanceof EntityValidationPluginEvaluationOptions)
                {
                    return evaluateEntityValidation(context, plugin.getName(), plugin.getPluginType(), plugin.getEntityKind(), plugin.getScript(),
                            (EntityValidationPluginEvaluationOptions) options);
                } else
                {
                    throw UserFailureException.fromTemplate("'%s' is an entity validation plugin. It requires '%s' evaluation options, but got '%s'",
                            options.getPluginId(), EntityValidationPluginEvaluationOptions.class.getSimpleName(),
                            options.getClass().getSimpleName());
                }
            default:
                throw UserFailureException.fromTemplate("Unsupported plugin type '%s'", plugin.getScriptType());
        }
    }

    private PluginEvaluationResult evaluatePluginFromScript(IOperationContext context, PluginEvaluationOptions options)
    {
        if (options instanceof DynamicPropertyPluginEvaluationOptions)
        {
            return evaluateDynamicProperty(context, null, ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType.JYTHON, null,
                    options.getPluginScript(), (DynamicPropertyPluginEvaluationOptions) options);
        } else if (options instanceof EntityValidationPluginEvaluationOptions)
        {
            return evaluateEntityValidation(context, null, ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType.JYTHON, null,
                    options.getPluginScript(), (EntityValidationPluginEvaluationOptions) options);
        } else
        {
            throw UserFailureException.fromTemplate("Unsupported plugin evaluation options '%s'", options.getClass().getSimpleName());
        }
    }

    public DynamicPropertyPluginEvaluationResult evaluateDynamicProperty(IOperationContext context, String pluginName,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType pluginType,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind pluginEntityKind, String pluginScript,
            DynamicPropertyPluginEvaluationOptions options)
    {
        IEntityInformationWithPropertiesHolder entity = getEntity(context, pluginEntityKind, options.getObjectId());

        try
        {
            IDynamicPropertyCalculator calculator =
                    dynamicPropertyCalculatorFactory.getCalculator(pluginType,
                            pluginName, pluginScript);
            IDynamicPropertyEvaluator evaluator =
                    new DynamicPropertyEvaluator(daoFactory, null,
                            dynamicPropertyCalculatorFactory, managedPropertyEvaluatorFactory);
            IEntityAdaptor adaptor =
                    EntityAdaptorFactory.create(entity, evaluator, daoFactory
                            .getSessionFactory().getCurrentSession());
            String value = calculator.eval(adaptor);
            return new DynamicPropertyPluginEvaluationResult(value);
        } catch (Throwable e)
        {
            if (options.getPluginId() != null)
            {
                throw UserFailureException.fromTemplate(e, "Evaluation of dynamic property plugin '%s' failed.", options.getPluginId());
            } else
            {
                throw UserFailureException.fromTemplate(e, "Evaluation of dynamic property plugin failed.");
            }
        }
    }

    public EntityValidationPluginEvaluationResult evaluateEntityValidation(IOperationContext context, String pluginName,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType pluginType,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind pluginEntityKind,
            String pluginScript, EntityValidationPluginEvaluationOptions options)
    {
        IEntityInformationWithPropertiesHolder entity = getEntity(context, pluginEntityKind, options.getObjectId());
        try
        {
            final Collection<IObjectId> requestedValidations = new LinkedHashSet<>();

            IEntityValidator entityValidator =
                    entityValidationFactory.createEntityValidator(pluginType,
                            pluginName, pluginScript);
            entityValidator.init(new IValidationRequestDelegate<INonAbstractEntityAdapter>()
                {
                    @Override
                    public void requestValidation(INonAbstractEntityAdapter entityAdaptor)
                    {
                        IEntityInformationWithPropertiesHolder entity =
                                entityAdaptor.entityPE();
                        IObjectId id = null;

                        if (entity instanceof ExperimentPE)
                        {
                            id = new ExperimentIdentifier(entity.getIdentifier());
                        } else if (entity instanceof SamplePE)
                        {
                            id = new SampleIdentifier(entity.getIdentifier());
                        } else if (entity instanceof DataPE)
                        {
                            id = new DataSetPermId(((DataPE) entity).getCode());
                        } else if (entity instanceof MaterialPE)
                        {
                            id = new MaterialPermId(((MaterialPE) entity).getCode(), ((MaterialPE) entity).getEntityType().getCode());
                        }

                        requestedValidations.add(id);
                    }
                });

            IDynamicPropertyEvaluator evaluator =
                    new DynamicPropertyEvaluator(daoFactory, null,
                            dynamicPropertyCalculatorFactory, managedPropertyEvaluatorFactory);
            IEntityAdaptor adaptor =
                    EntityAdaptorFactory.create(entity, evaluator, daoFactory
                            .getSessionFactory().getCurrentSession());
            String error = entityValidator.validate(adaptor, options.isNew());

            return new EntityValidationPluginEvaluationResult(error, requestedValidations);

        } catch (Throwable e)
        {
            if (options.getPluginId() != null)
            {
                throw UserFailureException.fromTemplate(e, "Evaluation of entity validation plugin '%s' failed.", options.getPluginId());
            } else
            {
                throw UserFailureException.fromTemplate(e, "Evaluation of entity validation plugin failed.");
            }
        }
    }

    private IEntityInformationWithPropertiesHolder getEntity(IOperationContext context,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind pluginEntityKind, IObjectId objectId)
    {
        if (objectId == null)
        {
            throw new UserFailureException("Object id cannot be null");
        }

        Map<? extends IObjectId, ? extends IEntityInformationWithPropertiesHolder> objectMap = null;
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind objectEntityKind = null;

        if (objectId instanceof IExperimentId)
        {
            objectMap = mapExperimentByIdExecutor.map(context, Arrays.asList((IExperimentId) objectId));
            objectEntityKind = ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.EXPERIMENT;
        } else if (objectId instanceof ISampleId)
        {
            objectMap = mapSampleByIdExecutor.map(context, Arrays.asList((ISampleId) objectId));
            objectEntityKind = ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.SAMPLE;
        } else if (objectId instanceof IDataSetId)
        {
            objectMap = mapDataSetByIdExecutor.map(context, Arrays.asList((IDataSetId) objectId));
            objectEntityKind = ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.DATA_SET;
        } else if (objectId instanceof IMaterialId)
        {
            objectMap = mapMaterialByIdExecutor.map(context, Arrays.asList((IMaterialId) objectId));
            objectEntityKind = ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.MATERIAL;
        } else
        {
            throw new UserFailureException("Unsupported objectId: " + objectId);
        }

        IEntityInformationWithPropertiesHolder object = objectMap.get(objectId);

        if (object == null)
        {
            throw new ObjectNotFoundException(objectId);
        }

        if (pluginEntityKind != null && pluginEntityKind != objectEntityKind)
        {
            throw UserFailureException
                    .fromTemplate(
                            "Cannot evaluate a plugin for object '%s'. The plugin expects objects with entity kind '%s' while the object has entity kind '%s'",
                            objectId, pluginEntityKind, objectEntityKind);
        }

        return object;
    }

    private ScriptPE getPlugin(IOperationContext context, IPluginId pluginId)
    {
        Map<IPluginId, ScriptPE> plugins = mapPluginByIdExecutor.map(context, Arrays.asList(pluginId));
        return plugins.get(pluginId);
    }

}
