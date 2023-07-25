/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertiesBean;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IPerson;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ValidationException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyFunctions;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IManagedPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;

/**
 * Handles Managed Properties of batch uploads/updates.
 *
 * @author Franz-Josef Elmer
 */
public class PropertiesBatchManager implements IPropertiesBatchManager
{

    private static class EvaluationContext
    {
        IManagedPropertyEvaluator evaluator;

        ScriptPE scriptPEorNull;
    }

    private final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, getClass());

    private final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public PropertiesBatchManager(IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    @Override
    public void manageProperties(SampleTypePE sampleType, List<NewSample> samples,
            PersonPE registrator)
    {
        Set<? extends EntityTypePropertyTypePE> sampleTypePropertyTypes =
                sampleType.getSampleTypePropertyTypes();

        managePropertiesBeans(samples, sampleTypePropertyTypes, registrator);
    }

    @Override
    public void manageProperties(ExperimentTypePE experimentType,
            List<? extends NewBasicExperiment> experiments, PersonPE registrator)
    {
        Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes =
                experimentType.getExperimentTypePropertyTypes();

        managePropertiesBeans(experiments, entityTypePropertyTypes, registrator);
    }

    @Override
    public void manageProperties(MaterialTypePE materialType, List<NewMaterial> materials,
            PersonPE registrator)
    {
        Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes =
                materialType.getMaterialTypePropertyTypes();
        managePropertiesBeans(materials, entityTypePropertyTypes, registrator);
    }

    @Override
    public void manageProperties(DataSetTypePE dataSetType, List<NewDataSet> dataSets,
            PersonPE registrator)
    {
        Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes =
                dataSetType.getDataSetTypePropertyTypes();
        managePropertiesBeans(dataSets, entityTypePropertyTypes, registrator);
    }

    private void managePropertiesBeans(List<? extends IPropertiesBean> propertiesBeans,
            Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes, PersonPE registrator)
    {
        Map<String, EvaluationContext> contexts = createEvaluationContexts(entityTypePropertyTypes);
        PropertiesBatchEvaluationErrors errors =
                new PropertiesBatchEvaluationErrors(registrator, propertiesBeans.size());
        IPerson person = PersonTranslator.translateToIPerson(registrator);

        int rowNumber = 0;
        for (IPropertiesBean propertiesBean : propertiesBeans)
        {
            rowNumber++;
            List<IEntityProperty> newProperties =
                    accumulateNewProperties(propertiesBean, person, rowNumber, contexts, errors);
            IEntityProperty[] newPropArray =
                    newProperties.toArray(new IEntityProperty[newProperties.size()]);
            propertiesBean.setProperties(newPropArray);
        }

        if (errors.hasErrors())
        {
            // send an email, so that actions can be taken to repair the script
            notificationLog.error(errors.constructErrorReportEmail());
            // inform the user that batch import has failed
            throw new UserFailureException(errors.constructUserFailureMessage());
        }
    }

    private List<IEntityProperty> accumulateNewProperties(IPropertiesBean propertiesBean,
            IPerson person, int rowNumber, Map<String, EvaluationContext> contexts,
            PropertiesBatchEvaluationErrors errors)
    {
        List<IEntityProperty> newProperties = new ArrayList<IEntityProperty>();

        Map<String, Map<String, String>> subColumnBindings =
                createColumnBindingsMap(propertiesBean.getProperties(), contexts);
        for (Entry<String, Map<String, String>> entry : subColumnBindings.entrySet())
        {
            String code = entry.getKey();
            EvaluationContext evalContext = contexts.get(code);
            try
            {
                EntityProperty entityProperty =
                        evaluateManagedProperty(code, person, entry.getValue(), evalContext);
                if (false == ManagedProperty.isSpecialValue(entityProperty.getStringValue()))
                {
                    newProperties.add(entityProperty);
                }
            } catch (EvaluatorException ex)
            {
                Throwable cause = ex.getCause();
                if (cause instanceof ValidationException)
                {
                    throw new UserFailureException("Error in row " + rowNumber + ": "
                            + cause.getMessage());
                }
                errors.accumulateError(rowNumber, ex, code, evalContext.scriptPEorNull);
            }
        }

        return newProperties;
    }

    private EntityProperty evaluateManagedProperty(String code, IPerson person,
            Map<String, String> bindings, EvaluationContext evalContext)
    {
        EntityProperty entityProperty = createNewEntityProperty(code);
        if (evalContext == null)
        {
            entityProperty.setValue(bindings.get(""));
        } else
        {
            IManagedPropertyEvaluator evaluator = evalContext.evaluator;
            ManagedProperty managedProperty = new ManagedProperty();
            managedProperty.setPropertyTypeCode(code);
            evaluator.updateFromBatchInput(managedProperty, person, bindings);
            entityProperty.setValue(managedProperty.getValue());
        }
        return entityProperty;
    }

    private EntityProperty createNewEntityProperty(String code)
    {
        EntityProperty entityProperty = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(code);
        propertyType.setDataType(new DataType(DataTypeCode.VARCHAR));
        entityProperty.setPropertyType(propertyType);
        return entityProperty;
    }

    private Map<String, Map<String, String>> createColumnBindingsMap(IEntityProperty[] properties,
            Map<String, EvaluationContext> contexts)
    {
        Map<String, Map<String, String>> subColumnBindings =
                new HashMap<String, Map<String, String>>();

        Map<String, String> originalColumnBindings = new HashMap<String, String>();
        for (IEntityProperty property : properties)
        {
            final String code = property.getPropertyType().getCode().toUpperCase();
            final String value = property.getStringValue();
            originalColumnBindings.put(ManagedPropertyFunctions.originalColumnNameBindingKey(code),
                    value);

            int indexOfColon = code.indexOf(':');
            String propertyCode = code;
            String subColumn = "";
            if (indexOfColon >= 0)
            {
                propertyCode = code.substring(0, indexOfColon);
                subColumn = code.substring(indexOfColon + 1);
            }
            Map<String, String> bindings = subColumnBindings.get(propertyCode);
            if (bindings == null)
            {
                bindings = new HashMap<String, String>();
                subColumnBindings.put(propertyCode, bindings);
            }
            bindings.put(subColumn, value);
        }
        // add original column bindings to all bindings
        for (Map<String, String> bindings : subColumnBindings.values())
        {
            for (Entry<String, String> originalColumnEntry : originalColumnBindings.entrySet())
            {
                bindings.put(originalColumnEntry.getKey(), originalColumnEntry.getValue());
            }
        }

        for (Entry<String, EvaluationContext> entry : contexts.entrySet())
        {
            String code = entry.getKey().toUpperCase();
            if (false == subColumnBindings.containsKey(code))
            {
                subColumnBindings.put(code, new HashMap<String, String>(originalColumnBindings));
            }
        }
        return subColumnBindings;
    }

    private Map<String, EvaluationContext> createEvaluationContexts(
            Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes)
    {
        Map<String, EvaluationContext> result = new HashMap<String, EvaluationContext>();
        for (EntityTypePropertyTypePE entityTypePropertyType : entityTypePropertyTypes)
        {
            if (entityTypePropertyType.isManaged())
            {
                String propertyTypeCode = entityTypePropertyType.getPropertyType().getCode();
                EvaluationContext context = new EvaluationContext();
                context.evaluator =
                        managedPropertyEvaluatorFactory
                                .createManagedPropertyEvaluator(entityTypePropertyType);
                context.scriptPEorNull = entityTypePropertyType.getScript();
                result.put(propertyTypeCode, context);
            }
        }
        return result;
    }
}
