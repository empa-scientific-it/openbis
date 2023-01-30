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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.EXPRESSION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.IS_PUBLIC;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.REGISTRATOR;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExpression;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Super class of common code for providers of subclasses of {@link AbstractExpression}.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExpressionProvider<T extends AbstractExpression> extends
        AbstractCommonTableModelProvider<T>
{
    protected final String gridId;

    public AbstractExpressionProvider(ICommonServer commonServer, String sessionToken, String gridId)
    {
        super(commonServer, sessionToken);
        this.gridId = gridId;
    }

    @Override
    protected TypedTableModel<T> createTableModel()
    {
        List<T> expressions = listExpressions();
        TypedTableModelBuilder<T> builder = new TypedTableModelBuilder<T>();
        addAdditionalColumn(builder);
        builder.addColumn(NAME);
        builder.addColumn(DESCRIPTION);
        builder.addColumn(EXPRESSION).hideByDefault();
        builder.addColumn(IS_PUBLIC).hideByDefault();
        builder.addColumn(REGISTRATOR).hideByDefault();
        builder.addColumn(REGISTRATION_DATE).hideByDefault();
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        for (T expression : expressions)
        {
            builder.addRow(expression);
            addAdditionalColumnValue(builder, expression);
            builder.column(NAME).addString(expression.getName());
            builder.column(DESCRIPTION).addString(expression.getDescription());
            builder.column(EXPRESSION).addString(expression.getExpression());
            builder.column(IS_PUBLIC).addString(SimpleYesNoRenderer.render(expression.isPublic()));
            builder.column(REGISTRATOR).addPerson(expression.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(expression.getRegistrationDate());
            builder.column(MODIFICATION_DATE).addDate(expression.getModificationDate());
        }
        return builder.getModel();
    }

    protected void addAdditionalColumn(TypedTableModelBuilder<T> builder)
    {
    }

    protected void addAdditionalColumnValue(TypedTableModelBuilder<T> builder, T expression)
    {
    }

    protected abstract List<T> listExpressions();

}
