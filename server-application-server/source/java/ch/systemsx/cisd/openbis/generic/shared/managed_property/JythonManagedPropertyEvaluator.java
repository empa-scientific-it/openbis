/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedUiActionDescriptionFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IPerson;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IManagedPropertyEvaluator;

/**
 * Class for evaluating scripts that control managed properties.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonManagedPropertyEvaluator implements IManagedPropertyEvaluator
{
    private static final class UniqunessChecker
    {
        private final Set<String> codes = new HashSet<String>();

        private final String type;

        private final String codeName;

        UniqunessChecker(String type, String codeName)
        {
            this.type = type;
            this.codeName = codeName;
        }

        void check(String code)
        {
            if (codes.add(code) == false)
            {
                throw new EvaluatorException("There is already " + type + " with " + codeName
                        + ": " + code);
            }
        }
    }

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JythonManagedPropertyEvaluator.class);

    /**
     * The name of the function that creates ui description
     */
    private static final String CONFIGURE_UI_FUNCTION = "configureUI";

    /**
     * The name of the function that updates the value of managed property from ui input fields
     */
    private static final String UPDATE_FROM_UI_FUNCTION = "updateFromUI";

    /**
     * The name of the function that returns an array of column names.
     */
    private static final String BATCH_COLUMN_NAMES_FUNCTION = "batchColumnNames";

    /**
     * The name of the function that returns an array of @IManagedInputWidgetDescription.
     */
    private static final String INPUT_WIDGETS_FUNCTION = "inputWidgets";

    /**
     * The name of the function that expects a map of bindings.
     */
    private static final String UPDATE_FROM_BATCH_INPUT_FUNCTION = "updateFromBatchInput";

    /**
     * The name of the function that expects a list of maps of bindings.
     */
    private static final String UPDATE_FROM_REGISTRATION_FORM_FUNCTION =
            "updateFromRegistrationForm";

    private static final String PROPERTY_VARIABLE_NAME = "property";

    private static final String PROPERTY_PE_VARIABLE_NAME = "propertyPE";

    private static final String PERSON_VARIABLE_NAME = "person";

    private final IEvaluationRunner runner;

    private final List<String> columnNames;

    private final boolean updateFromBatchFunctionDefined;

    private final boolean updateFromRegistrationFormFunctionDefined;

    private List<IManagedInputWidgetDescription> inputWidgetDescriptions;

    public JythonManagedPropertyEvaluator(final String scriptExpression)
    {
        this(new IEvaluationRunner()
            {
                private IJythonEvaluator evaluator;

                {
                    this.evaluator =
                            Evaluator.getFactory().create("", ManagedPropertyFunctions.class, scriptExpression);
                }

                @Override
                public <T> T evaluate(IAtomicEvaluation<T> evaluation)
                {
                    return evaluation.evaluate(evaluator);
                }

            });
    }

    public JythonManagedPropertyEvaluator(IEvaluationRunner runner)
    {
        this.runner = runner;
        updateFromBatchFunctionDefined = runner.evaluate(new IAtomicEvaluation<Boolean>()
            {
                @Override
                public Boolean evaluate(IJythonEvaluator evaluator)
                {
                    return evaluator.hasFunction(UPDATE_FROM_BATCH_INPUT_FUNCTION);
                }
            });

        updateFromRegistrationFormFunctionDefined =
                runner.evaluate(new IAtomicEvaluation<Boolean>()
                    {
                        @Override
                        public Boolean evaluate(IJythonEvaluator evaluator)
                        {
                            return evaluator.hasFunction(UPDATE_FROM_REGISTRATION_FORM_FUNCTION);
                        }
                    });

        boolean batchColumnNamesFunctionDefined = runner.evaluate(new IAtomicEvaluation<Boolean>()
            {
                @Override
                public Boolean evaluate(IJythonEvaluator evaluator)
                {
                    return evaluator.hasFunction(BATCH_COLUMN_NAMES_FUNCTION);
                }
            });

        boolean inputWidgetsFunctionDefined = runner.evaluate(new IAtomicEvaluation<Boolean>()
            {
                @Override
                public Boolean evaluate(IJythonEvaluator evaluator)
                {
                    return evaluator.hasFunction(INPUT_WIDGETS_FUNCTION);
                }
            });

        checkCombinationsOfDefinedFunctions(batchColumnNamesFunctionDefined,
                inputWidgetsFunctionDefined);
        columnNames = new ArrayList<String>();
        inputWidgetDescriptions = new ArrayList<IManagedInputWidgetDescription>();
        if (inputWidgetsFunctionDefined)
        {
            List<?> widgets = evalFunction(INPUT_WIDGETS_FUNCTION);
            UniqunessChecker uniqunessChecker = new UniqunessChecker("an input widget", "code");
            for (int i = 0; i < widgets.size(); i++)
            {
                Object widget = widgets.get(i);
                if (widget == null)
                {
                    throw new EvaluatorException("Function " + INPUT_WIDGETS_FUNCTION
                            + " has returned a list where the " + (i + 1) + ". element is null.");
                }
                if (widget instanceof IManagedInputWidgetDescription == false)
                {
                    throw new EvaluatorException("Function " + INPUT_WIDGETS_FUNCTION
                            + " has returned a list where the " + (i + 1)
                            + ". element isn't of type "
                            + IManagedInputWidgetDescription.class.getName() + " but "
                            + widget.getClass().getName() + ".");
                }
                IManagedInputWidgetDescription widgetDescription =
                        (IManagedInputWidgetDescription) widget;
                inputWidgetDescriptions.add(widgetDescription);

                if (batchColumnNamesFunctionDefined == false)
                {
                    String code = widgetDescription.getCode();
                    uniqunessChecker.check(code);
                    columnNames.add(code);
                }
            }
        }
        if (batchColumnNamesFunctionDefined)
        {
            List<?> list = evalFunction(BATCH_COLUMN_NAMES_FUNCTION);
            UniqunessChecker uniqunessChecker =
                    new UniqunessChecker("a batch column", "name in uppercase");
            ManagedUiActionDescriptionFactory descriptionFactory =
                    new ManagedUiActionDescriptionFactory();
            for (Object element : list)
            {
                String columnName = element.toString();
                String code = columnName.toUpperCase();
                uniqunessChecker.check(code);
                columnNames.add(code);
                if (inputWidgetsFunctionDefined == false)
                {
                    inputWidgetDescriptions
                            .add(descriptionFactory.createTextInputField(columnName));
                }
            }
        }
    }

    private void checkCombinationsOfDefinedFunctions(boolean batchColumnNamesFunctionDefined,
            boolean inputWidgetsFunctionDefined)
    {
        if (batchColumnNamesFunctionDefined && updateFromBatchFunctionDefined == false)
        {
            StringBuilder builder = new StringBuilder("Function ");
            builder.append(UPDATE_FROM_BATCH_INPUT_FUNCTION);
            builder.append(" is not defined although function ");
            builder.append(BATCH_COLUMN_NAMES_FUNCTION);
            builder.append(" is defined.");
            throw new EvaluatorException(builder.toString());
        }

        if (inputWidgetsFunctionDefined && updateFromRegistrationFormFunctionDefined == false)
        {
            StringBuilder builder = new StringBuilder("Function ");
            builder.append(UPDATE_FROM_REGISTRATION_FORM_FUNCTION);
            builder.append(" is not defined although function ");
            builder.append(INPUT_WIDGETS_FUNCTION);
            builder.append(" is defined.");
            throw new EvaluatorException(builder.toString());
        }
    }

    private List<?> evalFunction(final String functionName)
    {
        Object result = runner.evaluate(new IAtomicEvaluation<Object>()
            {
                @Override
                public Object evaluate(IJythonEvaluator evaluator)
                {
                    return evaluator.evalFunction(functionName);
                }
            });

        if (result instanceof List == false)
        {
            throw new EvaluatorException("Function '" + functionName
                    + "' doesn't return a List but an object of type '"
                    + result.getClass().getName() + "': " + result);
        }
        return (List<?>) result;
    }

    @Override
    public void configureUI(final IManagedProperty managedProperty,
            final IEntityPropertyAdaptor entityProperty)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property ui configuration'%s'.",
                    managedProperty));
        }

        runner.evaluate(new IAtomicEvaluation<Void>()
            {
                @Override
                public Void evaluate(IJythonEvaluator evaluator)
                {
                    evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
                    evaluator.set(PROPERTY_PE_VARIABLE_NAME, entityProperty);
                    evaluator.evalFunction(CONFIGURE_UI_FUNCTION);
                    return null;
                }
            });
    }

    @Override
    public void updateFromUI(final IManagedProperty managedProperty, final IPerson person,
            final IManagedUiAction action)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property value update '%s'.",
                    managedProperty));
        }

        runner.evaluate(new IAtomicEvaluation<Void>()
            {
                @Override
                public Void evaluate(IJythonEvaluator evaluator)
                {
                    evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
                    evaluator.set(PERSON_VARIABLE_NAME, person);
                    evaluator.evalFunction(UPDATE_FROM_UI_FUNCTION, action);
                    return null;
                }
            });
    }

    @Override
    public List<String> getBatchColumnNames()
    {
        return columnNames;
    }

    @Override
    public List<IManagedInputWidgetDescription> getInputWidgetDescriptions()
    {
        return inputWidgetDescriptions;
    }

    @Override
    public void updateFromBatchInput(final IManagedProperty managedProperty, final IPerson person,
            final Map<String, String> bindings)
    {
        if (updateFromBatchFunctionDefined == false)
        {
            if (bindings.containsKey(""))
            {
                managedProperty.setValue(bindings.get(""));
            }
        } else
        {
            runner.evaluate(new IAtomicEvaluation<Void>()
                {
                    @Override
                    public Void evaluate(IJythonEvaluator evaluator)
                    {
                        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
                        evaluator.set(PERSON_VARIABLE_NAME, person);
                        evaluator.evalFunction(UPDATE_FROM_BATCH_INPUT_FUNCTION, bindings);
                        return null;
                    }
                });
        }
    }

    @Override
    public void updateFromRegistrationForm(final IManagedProperty managedProperty,
            final IPerson person, final List<Map<String, String>> bindings)
    {
        if (updateFromRegistrationFormFunctionDefined)
        {
            runner.evaluate(new IAtomicEvaluation<Void>()
                {
                    @Override
                    public Void evaluate(IJythonEvaluator evaluator)
                    {
                        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
                        evaluator.set(PERSON_VARIABLE_NAME, person);
                        evaluator.evalFunction(UPDATE_FROM_REGISTRATION_FORM_FUNCTION, bindings);
                        return null;
                    }
                });
        }
    }
}
