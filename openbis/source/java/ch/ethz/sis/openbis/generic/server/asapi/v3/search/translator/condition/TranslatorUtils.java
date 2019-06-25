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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractNumberValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberGreaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberGreaterThanValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberLessThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberLessThanValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringStartsWithValue;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.BARS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.GE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.GT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LIKE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERCENT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SQ;

class TranslatorUtils
{

    private TranslatorUtils()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    static void appendStringComparatorOp(final AbstractStringValue value, final StringBuilder sqlBuilder)
    {
        if (value.getClass() == StringEqualToValue.class)
        {
            sqlBuilder.append(EQ).append(QU);
        } else if (value.getClass() == StringStartsWithValue.class)
        {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(QU).append(SP).append(BARS).append(SP).append(SQ).
                    append(PERCENT).append(SQ);
        } else if (value.getClass() == StringEndsWithValue.class)
        {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(SQ).append(PERCENT).append(SQ).append(SP).append(BARS).append(SP).append(QU);
        } else if (value.getClass() == StringContainsValue.class)
        {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(SQ).append(PERCENT).append(SQ).append(SP).append(BARS).append(SP).append(QU).
                    append(SP).append(BARS).append(SP).append(SQ).append(PERCENT).append(SQ);
        } else
        {
            throw new IllegalArgumentException("Unsupported AbstractStringValue type: " + value.getClass().getSimpleName());
        }
    }

    static void appendNumberComparatorOp(final AbstractNumberValue value, final StringBuilder sqlBuilder)
    {
        if (value.getClass() == NumberEqualToValue.class)
        {
            sqlBuilder.append(EQ);
        } else if (value.getClass() == NumberLessThanValue.class)
        {
            sqlBuilder.append(LT);
        } else if (value.getClass() == NumberLessThanOrEqualToValue.class)
        {
            sqlBuilder.append(LE);
        } else if (value.getClass() == NumberGreaterThanValue.class)
        {
            sqlBuilder.append(GT);
        } else if (value.getClass() == NumberGreaterThanOrEqualToValue.class)
        {
            sqlBuilder.append(GE);
        } else
        {
            throw new IllegalArgumentException("Unsupported AbstractNumberValue type: " + value.getClass().getSimpleName());
        }
        sqlBuilder.append(QU);
    }

}
