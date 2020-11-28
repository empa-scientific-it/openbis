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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

public class SQLLexemes
{

    public static final String ARRAY = "ARRAY";

    public static final String UNNEST = "UNNEST";

    public static final String COALESCE = "COALESCE";

    public static final String CONCAT = "CONCAT";

    public static final String LOWER = "LOWER";

    public static final String UPPER = "UPPER";

    public static final String GREATEST = "GREATEST";

    public static final String SUM = "SUM";

    public static final String COUNT = "COUNT";

    public static final String OVER = "OVER";

    public static final String ARRAY_POSITION = "array_position";

    public static final String DATE_TRUNC = "date_trunc";

    public static final String TS_RANK = "ts_rank";

    public static final String TSVECTOR_DOCUMENT = "tsvector_document";

    public static final String TO_TSQUERY = "to_tsquery";

    public static final String TO_TSVECTOR = "to_tsvector";

    public static final String TS_HEADLINE = "ts_headline";

    public static final String TSQUERY = "tsquery";

    public static final String PLAINTO_TSQUERY = "plainto_tsquery";

    public static final String AT_TIME_ZONE = "AT TIME ZONE";

    public static final String SELECT = "SELECT";

    public static final String INSERT = "INSERT";

    public static final String DELETE = "DELETE";

    public static final String DISTINCT = "DISTINCT";

    public static final String FROM = "FROM";

    public static final String INTO = "INTO";

    public static final String WHERE = "WHERE";

    public static final String HAVING = "HAVING";

    public static final String CASE = "CASE";

    public static final String WHEN = "WHEN";

    public static final String THEN = "THEN";

    public static final String ELSE = "ELSE";

    public static final String END = "END";

    public static final String LEFT = "LEFT";

    public static final String INNER = "INNER";

    public static final String JOIN = "JOIN";

    public static final String VALUES = "VALUES";

    public static final String SP = " ";

    public static final String LEFT_JOIN = LEFT + SP + JOIN;

    public static final String INNER_JOIN = INNER + SP + JOIN;

    public static final String LIKE = "LIKE";

    public static final String ILIKE = "ILIKE";

    public static final String ON = "ON";

    public static final String IN = "IN";

    public static final String IS = "IS";

    public static final String AS = "AS";

    public static final String NOT = "NOT";

    public static final String AND = "AND";

    public static final String OR = "OR";

    public static final String NULL = "NULL";

    public static final String TRUE = Boolean.TRUE.toString();

    public static final String FALSE = Boolean.FALSE.toString();

    public static final String IS_NOT_NULL = IS + SP + NOT + SP + NULL;

    public static final String IS_NULL = IS + SP + NULL;

    public static final String GROUP_BY = "GROUP BY";

    public static final String ORDER_BY = "ORDER BY";

    public static final String LIMIT = "LIMIT";

    public static final String OFFSET = "OFFSET";

    public static final String INTERSECT = "INTERSECT";

    public static final String UNION = "UNION";

    public static final String UNION_ALL = "UNION ALL";

    public static final String NL = "\n";

    public static final String COMMA = ",";

    public static final String PERIOD = ".";

    public static final String DOUBLE_COLON = "::";

    public static final String EQ = "=";

    public static final String GT = ">";

    public static final String LT = "<";

    public static final String GE = ">=";

    public static final String LE = "<=";

    public static final char ASTERISK = '*';

    public static final char QU = '?';

    public static final char UNDERSCORE = '_';

    public static final char PERCENT = '%';

    public static final char BACKSLASH = '\\';

    public static final String BARS = "||";

    public static final String LP = "(";

    public static final String RP = ")";

    public static final String LB = "[";

    public static final String RB = "]";

    public static final String SQ = "'";

    public static final String DQ = "\"";

    public static final String DOUBLE_AT = "@@";

    public static final String DOUBLE_DOLLAR = "$$";

    public static final String TILDA = "~";

    public static final String PLUS = "+";

    public static final String SELECT_UNNEST = LP + SELECT + SP + UNNEST + LP + QU + RP + RP;

    private SQLLexemes()
    {
        throw new UnsupportedOperationException();
    }

}
