/*
 * Copyright 2008 ETH Zuerich, CISD
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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ExpressionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExpression;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractExpressionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Grid custom filter or column translator.
 * 
 * @author Tomasz Pylak
 */
public final class GridCustomExpressionTranslator
{
    private GridCustomExpressionTranslator()
    {
        // Can not be instantiated.
    }

    /** A {@link GridCustomColumn} &lt;---&gt; {@link GridCustomColumnPE} translator. */
    public static class GridCustomColumnTranslator
    {
        public final static List<GridCustomColumn> translate(final List<GridCustomColumnPE> columns)
        {
            final List<GridCustomColumn> result = new ArrayList<GridCustomColumn>();
            for (final GridCustomColumnPE column : columns)
            {
                result.add(translate(column));
            }
            return result;
        }

        private final static GridCustomColumn translate(final GridCustomColumnPE original)
        {
            if (original == null)
            {
                return null;
            }
            final GridCustomColumn result = new GridCustomColumn();
            result.setCode(original.getCode());
            result.setName(original.getLabel());

            translateExpression(original, result);
            return result;
        }
    }

    /**
     * A {@link GridCustomFilter} &lt;---&gt; {@link GridCustomFilterPE} translator.
     * 
     * @author Izabela Adamczyk
     */
    public static final class GridCustomFilterTranslator
    {
        public final static List<GridCustomFilter> translate(final List<GridCustomFilterPE> filters)
        {
            final List<GridCustomFilter> result = new ArrayList<GridCustomFilter>();
            for (final GridCustomFilterPE filter : filters)
            {
                result.add(GridCustomFilterTranslator.translate(filter));
            }
            return result;
        }

        public final static GridCustomFilter translate(final GridCustomFilterPE original)
        {
            if (original == null)
            {
                return null;
            }
            final GridCustomFilter result = new GridCustomFilter();
            result.setName(original.getName());
            result.setupParameters(ExpressionUtil.extractParameters(original.getExpression()));

            translateExpression(original, result);
            return result;
        }

    }

    public static void translateExpression(final AbstractExpressionPE<?> expression,
            final AbstractExpression result)
    {
        result.setId(HibernateUtils.getId(expression));
        result.setModificationDate(expression.getModificationDate());
        result.setExpression(expression.getExpression());
        result.setDescription(expression.getDescription());
        result.setRegistrator(PersonTranslator.translate(expression.getRegistrator()));
        result.setRegistrationDate(expression.getRegistrationDate());
        result.setModificationDate(expression.getModificationDate());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate());
        result.setPublic(expression.isPublic());
    }
}
