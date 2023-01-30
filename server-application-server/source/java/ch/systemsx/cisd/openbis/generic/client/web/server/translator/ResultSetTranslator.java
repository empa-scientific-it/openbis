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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import ch.systemsx.cisd.common.string.ReflectingStringEscaper;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;

/**
 * A {@link ResultSet} &lt;---&gt; {@link IResultSet} translator.
 * 
 * @author Christian Ribeaud
 */
public final class ResultSetTranslator
{
    public enum Escape
    {
        YES, NO
    }

    private ResultSetTranslator()
    {
        // Can not be instantiated.
    }

    public final static <K, T> ResultSet<T> translate(final IResultSet<String, T> result,
            Escape escape)
    {
        final ResultSet<T> resultSet = new ResultSet<T>();
        final GridRowModels<T> resultSetList =
                escape == Escape.YES ? ReflectingStringEscaper.escapeDeepWithCopy(result.getList())
                        : result.getList();
        resultSet.setList(resultSetList);
        resultSet.setTotalLength(result.getTotalLength());
        resultSet.setResultSetKey(result.getResultSetKey());
        resultSet.setPartial(result.isPartial());
        return resultSet;
    }
}
