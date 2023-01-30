/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.translator;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumnDataType;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * @author Franz-Josef Elmer
 */
public class QueryTableModelTranslatorTest extends AssertJUnit
{
    private static final String STRING_COLUMN = "String";

    private static final String INTEGER_COLUMN = "Integer";

    private static final String DOUBLE_COLUMN = "Double";

    private static final String TIMESTAMP_COLUMN = "Timestamp";

    @Test
    public void test()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addFullHeader(STRING_COLUMN, INTEGER_COLUMN, DOUBLE_COLUMN, TIMESTAMP_COLUMN);
        IRowBuilder row = builder.addRow();
        row.setCell(STRING_COLUMN, "Hello");
        row.setCell(INTEGER_COLUMN, 42);
        row.setCell(DOUBLE_COLUMN, 42.25);
        row.setCell(TIMESTAMP_COLUMN, new Date(7000));

        QueryTableModel translatedModel = new QueryTableModelTranslator(builder.getTableModel()).translate();

        List<QueryTableColumn> columns = translatedModel.getColumns();
        assertEquals(STRING_COLUMN, columns.get(0).getTitle());
        assertEquals(QueryTableColumnDataType.STRING, columns.get(0).getDataType());
        assertEquals(INTEGER_COLUMN, columns.get(1).getTitle());
        assertEquals(QueryTableColumnDataType.LONG, columns.get(1).getDataType());
        assertEquals(DOUBLE_COLUMN, columns.get(2).getTitle());
        assertEquals(QueryTableColumnDataType.DOUBLE, columns.get(2).getDataType());
        assertEquals(TIMESTAMP_COLUMN, columns.get(3).getTitle());
        assertEquals(QueryTableColumnDataType.STRING, columns.get(3).getDataType());
        assertEquals(4, columns.size());

        List<Serializable[]> rows = translatedModel.getRows();
        assertEquals("Hello", rows.get(0)[0]);
        assertEquals(Long.valueOf(42), rows.get(0)[1]);
        assertEquals(Double.valueOf(42.25), rows.get(0)[2]);
        assertEquals("1970-01-01 01:00:07 +0100", rows.get(0)[3]);
        assertEquals(1, rows.size());
    }
}
