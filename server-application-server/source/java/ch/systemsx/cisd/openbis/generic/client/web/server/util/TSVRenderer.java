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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.openbis.generic.client.web.server.IBasicTableDataProvider;

/**
 * Creates list of lines with tab separated columns;
 * 
 * @author Tomasz Pylak
 */
public class TSVRenderer
{
    private static final String TAB = "\t";

    private final String lineSeparator;

    /**
     * @param dataProvider Provider of headers and values
     * @param lineSeparator character used as a lineSeparator separator
     */
    public static String createTable(IBasicTableDataProvider dataProvider, String lineSeparator)
    {
        return new TSVRenderer(lineSeparator).createTable(dataProvider);
    }

    private String createTable(IBasicTableDataProvider dataProvider)
    {
        StringBuffer sb = new StringBuffer();
        appendHeader(dataProvider.getAllColumnTitles(), sb);
        List<List<? extends Comparable<?>>> rows = dataProvider.getRows();
        for (List<? extends Comparable<?>> row : rows)
        {
            appendEntity(row, sb);
        }
        return sb.toString();
    }

    private TSVRenderer(String lineSeparator)
    {
        this.lineSeparator = lineSeparator;
    }

    private void appendEntity(List<? extends Comparable<?>> row, StringBuffer sb)
    {
        boolean isFirst = true;
        for (Comparable<?> value : row)
        {
            if (isFirst == false)
            {
                sb.append(TAB);
            } else
            {
                isFirst = false;
            }
            sb.append(cleanWhitespaces(value == null ? "" : value.toString()));
        }
        sb.append(lineSeparator);
    }

    /**
     * @return <var>value</var> with white-spaces cleaned in the same way that HTML works (all contiguous white-spaces are replaced with single space)
     */
    private String cleanWhitespaces(String value)
    {
        String[] tokens = StringUtils.split(value);
        String result = StringUtils.join(tokens, " ");
        return result;
    }

    private void appendHeader(List<String> headers, StringBuffer sb)
    {
        boolean isFirst = true;
        for (String header : headers)
        {
            if (isFirst == false)
            {
                sb.append(TAB);
            } else
            {
                isFirst = false;
            }
            sb.append(header);
        }
        sb.append(lineSeparator);
    }
}
