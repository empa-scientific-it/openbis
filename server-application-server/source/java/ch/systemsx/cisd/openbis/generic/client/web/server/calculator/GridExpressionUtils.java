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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.util.DataTypeUtils;

/**
 * Utility class containing functions helpful with dealing with grid custom filters or columns.
 * 
 * @author Izabela Adamczyk
 */
// TODO 2010-02-16, CR: Instead of a bunch of static methods, this should be refactored using the
// Command pattern to GridExpressionFilterCommand and GridExpressionColumnCommand. The arguments to
// the functions should become ivars.
public class GridExpressionUtils
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            GridExpressionUtils.class);

    private static final String FILTER_EVALUATION_ERROR_MSG =
            "Problem occured during applying the filter.<br><br>Check that all provided parameter values are correct. "
                    + "If everything seems fine contact filter registrator or instance admin about a possible bug in the filter definition.";

    private static final String EVALUATION_SERIOUS_ERROR_MSG = "Serious problem occured during ";

    private static final String FILTER_EVALUATION_SERIOUS_ERROR_MSG = EVALUATION_SERIOUS_ERROR_MSG
            + "applying the filter: ";

    private static final String COLUMN_EVALUATION_ERROR_TEMPLATE_SHORT =
            "Error. Please contact '%s', who defined this column.";

    private static final String COLUMN_EVALUATION_ERROR_LONG = "Error: (%s).";

    /**
     * Applies the filter described by <code>customFilterInfo</code> to all rows and returns indices of all rows passing the filter.
     * 
     * @param dataProvider Provider of data and meta-data
     */
    public static List<Integer> applyCustomFilter(ITableDataProvider dataProvider,
            CustomFilterInfo<?> customFilterInfo)
    {
        List<Integer> filtered = new ArrayList<Integer>();
        String expression = StringEscapeUtils.unescapeHtml4(customFilterInfo.getExpression());
        Set<ParameterWithValue> parameters = customFilterInfo.getParameters();
        try
        {
            RowCalculator calculator = new RowCalculator(dataProvider, expression, parameters);
            List<List<? extends Comparable<?>>> rows = dataProvider.getRows();
            for (int i = 0; i < rows.size(); i++)
            {
                List<? extends Comparable<?>> row = rows.get(i);
                calculator.setRowData(row);
                if (calculator.evalToBoolean())
                {
                    filtered.add(i);
                }
            }
        } catch (Exception ex)
        {
            throw createInvalidFilterException(ex);
        }
        return filtered;
    }

    private static UserFailureException createInvalidFilterException(Exception ex)
    {
        String msg;
        String details = null;
        if (ex instanceof EvaluatorException)
        {
            msg = FILTER_EVALUATION_ERROR_MSG;
            details = ex.getMessage();
        } else
        {
            msg = FILTER_EVALUATION_SERIOUS_ERROR_MSG + ex;
        }
        logColumnCalculationError(ex, msg, details);
        return new UserFailureException(msg, details);
    }

    // Side effect: data type of customColumn is set
    public static List<PrimitiveValue> evalCustomColumn(ITableDataProvider dataProvider,
            GridCustomColumn customColumn,
            boolean errorMessagesAreLong)
    {
        RowCalculator calculator =
                createRowCalculator(dataProvider, customColumn, errorMessagesAreLong);
        List<PrimitiveValue> values = new ArrayList<PrimitiveValue>();
        List<List<? extends Comparable<?>>> rows = dataProvider.getRows();
        for (List<? extends Comparable<?>> row : rows)
        {
            PrimitiveValue value =
                    evalCustomColumn(row, customColumn, calculator, errorMessagesAreLong);
            if (value != PrimitiveValue.NULL)
            {
                customColumn.setDataType(DataTypeUtils.getCompatibleDataType(
                        customColumn.getDataType(), value.getDataType()));
            }
            values.add(value);
        }
        return values;
    }

    private static RowCalculator createRowCalculator(ITableDataProvider dataProvider,
            GridCustomColumn customColumn, boolean errorMessagesAreLong)
    {
        String expression = StringEscapeUtils.unescapeHtml4(customColumn.getExpression());
        try
        {
            return new RowCalculator(dataProvider, expression);
        } catch (Exception ex)
        {
            // if a column definition is faulty than we replace the original expression with the one
            // which always evaluates to an error message
            String msg =
                    createCustomColumnErrorMessage(customColumn, errorMessagesAreLong, ex).replace(
                            "'", "\\'");
            return new RowCalculator(dataProvider, "'" + msg + "'");
        }
    }

    private static PrimitiveValue evalCustomColumn(List<? extends Comparable<?>> rowData, GridCustomColumn customColumn,
            RowCalculator calculator, boolean errorMessagesAreLong)
    {
        try
        {
            calculator.setRowData(rowData);

            return calculator.getTypedResult();
        } catch (Exception ex)
        {
            return new PrimitiveValue(createCustomColumnErrorMessage(customColumn,
                    errorMessagesAreLong, ex));
        }
    }

    private static String createCustomColumnErrorMessage(GridCustomColumn customColumn,
            boolean errorMessagesAreLong, Exception ex)
    {
        String msg;
        Person registrator = customColumn.getRegistrator();
        String creator = registrator + " <" + registrator.getEmail() + ">";

        if (errorMessagesAreLong)
        {
            msg = String.format(COLUMN_EVALUATION_ERROR_LONG, ex.getMessage());
        } else
        {
            msg = String.format(COLUMN_EVALUATION_ERROR_TEMPLATE_SHORT, creator);
        }

        return msg;
    }

    private static void logColumnCalculationError(Exception ex, String msg, String details)
    {
        if (operationLog.isInfoEnabled())
        {
            // we do not log this as an error, because it can be only user fault (wrong column
            // definition)
            operationLog.info(msg + " DETAILS: " + details, ex);
        }
    }
}
