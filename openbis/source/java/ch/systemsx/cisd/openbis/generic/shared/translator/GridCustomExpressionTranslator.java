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

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExpression;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractExpressionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.generic.shared.util.ExpressionUtil;
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
            result.setCode(escapeHtml(original.getCode()));
            result.setName(escapeHtml(original.getLabel()));

            translateGridExpression(original, result);
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
            result.setName(escapeHtml(original.getName()));
            result.setParameters(ExpressionUtil.extractParameters(original.getExpression()));

            translateGridExpression(original, result);
            return result;
        }

    }

    /**
     * A {@link GridCustomFilter} &lt;---&gt; {@link QueryPE} translator.
     * 
     * @author Izabela Adamczyk
     */
    public static final class QueryTranslator
    {
        public final static List<GridCustomFilter> translate(final List<QueryPE> queries)
        {
            final List<GridCustomFilter> result = new ArrayList<GridCustomFilter>();
            for (final QueryPE query : queries)
            {
                result.add(QueryTranslator.translate(query));
            }
            return result;
        }
        
        public final static GridCustomFilter translate(final QueryPE original)
        {
            if (original == null)
            {
                return null;
            }
            final GridCustomFilter result = new GridCustomFilter();
            result.setName(escapeHtml(original.getName()));
            result.setParameters(ExpressionUtil.extractParameters(original.getExpression()));
            
            translateGridExpression(original, result);
            return result;
        }
        
    }
    
    private static void translateGridExpression(final AbstractExpressionPE<?> gridExpression,
            final AbstractExpression result)
    {
        result.setId(HibernateUtils.getId(gridExpression));
        result.setModificationDate(gridExpression.getModificationDate());
        result.setExpression(escapeHtml(gridExpression.getExpression()));
        result.setDescription(StringEscapeUtils.escapeHtml(gridExpression.getDescription()));
        result.setRegistrator(PersonTranslator.translate(gridExpression.getRegistrator()));
        result.setRegistrationDate(gridExpression.getRegistrationDate());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(gridExpression
                .getDatabaseInstance()));
        result.setPublic(gridExpression.isPublic());
    }

}
