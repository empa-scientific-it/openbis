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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.calculator.AbstractCalculator;

/**
 * @author Franz-Josef Elmer
 */
public class RowCalculator extends AbstractCalculator
{
    private final Row row;

    public RowCalculator(ITableDataProvider provider, String expression)
    {
        this(provider, expression, Collections.<ParameterWithValue> emptySet());
    }

    public RowCalculator(ITableDataProvider provider, String expression,
            Set<ParameterWithValue> parameters)
    {
        super(Evaluator.getFactory().create(substitudeParameters(expression, parameters), Math.class,
                getBasicInitialScript()));
        row = new Row(provider);
        evaluator.set("row", row);
    }

    public void setRowData(List<? extends Comparable<?>> rowValues)
    {
        row.setRowData(rowValues);
    }

    private static String substitudeParameters(String originalExpression,
            Set<ParameterWithValue> parameters)
    {
        Template template = new Template(originalExpression);
        for (ParameterWithValue pw : parameters)
        {
            template.bind(pw.getParameter(), pw.getValue());
        }
        return template.createText();
    }

}
