/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyValueValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PropertyValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.DynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * Default implementation of {@link IDynamicPropertyEvaluator}. For efficient evaluation of
 * properties a cache of compiled script is used internally.
 * 
 * @author Piotr Buczek
 */
public class DynamicPropertyEvaluator implements IDynamicPropertyEvaluator
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DynamicPropertyEvaluator.class);

    private static final IPropertyValueValidator validator = new PropertyValidator();

    private static final String ERROR_PREFIX = "ERROR: ";

    /** cache of calculators with precompiled expressions */
    private final Map<ScriptPE, DynamicPropertyCalculator> calculatorsByScript =
            new HashMap<ScriptPE, DynamicPropertyCalculator>();

    /** Returns a calculator for given script (creates a new one if nothing is found in cache). */
    private DynamicPropertyCalculator getCalculator(ScriptPE scriptPE)
    {
        // Creation of a calculator involves takes time because of compilation of the script.
        // That is why a cache is used.
        DynamicPropertyCalculator result = calculatorsByScript.get(scriptPE);
        if (result == null)
        {
            result = DynamicPropertyCalculator.create(scriptPE.getScript());
            calculatorsByScript.put(scriptPE, result);
        }
        return result;
    }

    public <T extends IEntityInformationWithPropertiesHolder> void evaluateProperties(T entity)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating dynamic properties of entity '%s'.",
                    entity));
        }
        for (EntityPropertyPE property : entity.getProperties())
        {
            EntityTypePropertyTypePE etpt = property.getEntityTypePropertyType();
            if (etpt.isDynamic())
            {
                try
                {
                    final DynamicPropertyCalculator calculator = getCalculator(etpt.getScript());
                    final IEntityAdaptor entityAdaptor = EntityAdaptorFactory.create(entity);
                    calculator.setEntity(entityAdaptor);
                    final String dynamicValue = calculator.evalAsString();
                    final String validatedValue =
                            validator.validatePropertyValue(etpt.getPropertyType(), dynamicValue);
                    property.setValue(validatedValue);
                } catch (Exception e)
                {
                    String errorMsg = ERROR_PREFIX + e.getMessage();
                    operationLog.info(errorMsg);
                    property.setValue(BasicConstant.ERROR_PROPERTY_PREFIX + errorMsg);
                }
            }
        }
    }
}
