/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;

/**
 * A text area to specify SQL query.
 * 
 * @author Piotr Buczek
 */
public class SQLQueryField extends MultilineVarcharField
{

    private final static String EMPTY_TEXT = "SELECT ... \nFROM ... \nWHERE ...";

    private final static String EMPTY_TEXT_WITH_KEY = EMPTY_TEXT + " '${key}' ...";

    private final static String EMPTY_TEXT_WITH_KEY_AND_TYPE = EMPTY_TEXT_WITH_KEY
            + " ... '${type}'";

    private static final String TYPE_REGEX = "\\$\\{type(::[^{}]+){0,1}\\}";

    private static final String KEY_REGEX = "\\$\\{key(::[^{}]+){0,1}\\}";

    private static final String ANY_REGEX = "(.|[\\n\\r])*";

    // SQL query needs to start with 'select' (ignore-case) and can contain newlines.
    private final static String SELECT_REGEX = "^(select|SELECT)" + ANY_REGEX;

    private static final String SELECT_WITH_KEY_REGEX = SELECT_REGEX + KEY_REGEX + ANY_REGEX;

    private static final String SELECT_WITH_KEY_AND_TYPE_REGEX = SELECT_WITH_KEY_REGEX + TYPE_REGEX
            + ANY_REGEX;

    private final static String REGEX_TEXT_MSG = "SQL query should begin with a 'SELECT' keyword.";

    private final static String REGEX_TEXT_WITH_KEY_MSG = REGEX_TEXT_MSG
            + " Magic parameter '${key}' is required.";

    private final static String REGEX_TEXT_WITH_KEY_AND_TYPE_MSG = REGEX_TEXT_WITH_KEY_MSG
            + " Magic parameter '${type}' required.";

    private final static String SINGLE_QUERY_MSG =
            "SQL query should not contain ';' in the middle.";

    private final static String BLANK_TEXT_MSG = "SQL query text required";

    public SQLQueryField(IMessageProvider messageProvider, boolean mandatory, int lines)
    {
        super(messageProvider.getMessage(Dict.SQL_QUERY), mandatory, lines);
        updateQueryType(QueryType.GENERIC);
        treatTabKeyAsInput();
    }

    public SQLQueryField(IMessageProvider messageProvider, boolean mandatory)
    {
        this(messageProvider, mandatory, DEFAULT_LINES);
    }

    /** {@link Validator} for single SQL query. */
    protected class SingleSQLQueryValidator implements Validator
    {

        @Override
        public String validate(Field<?> field, final String fieldValue)
        {
            int indexOfSemicolon = fieldValue.trim().indexOf(';');
            if (indexOfSemicolon >= 0 && indexOfSemicolon < fieldValue.length() - 1)
            {
                return SINGLE_QUERY_MSG;
            }
            // validated value is valid
            return null;
        }
    }

    public void updateQueryType(QueryType type)
    {
        setEmptyText(createEmptyText(type));
        setRegex(createRegex(type));
        setValidator(new SingleSQLQueryValidator());
        getMessages().setRegexText(createRegexText(type));
        getMessages().setBlankText(createBlankText(type));
        validate();
    }

    private static String createBlankText(QueryType type)
    {
        return BLANK_TEXT_MSG;
    }

    private static String createRegexText(QueryType type)
    {
        switch (type)
        {
            case GENERIC:
                return REGEX_TEXT_MSG;
            case DATA_SET:
            case SAMPLE:
            case EXPERIMENT:
                return REGEX_TEXT_WITH_KEY_MSG;
            case MATERIAL:
                return REGEX_TEXT_WITH_KEY_AND_TYPE_MSG;
        }
        return null;
    }

    private static String createRegex(QueryType type)
    {
        switch (type)
        {
            case GENERIC:
                return SELECT_REGEX;
            case DATA_SET:
            case SAMPLE:
            case EXPERIMENT:
                return SELECT_WITH_KEY_REGEX;
            case MATERIAL:
                return SELECT_WITH_KEY_AND_TYPE_REGEX;
        }
        return null;
    }

    private static String createEmptyText(QueryType type)
    {
        switch (type)
        {
            case GENERIC:
                return EMPTY_TEXT;
            case DATA_SET:
            case SAMPLE:
            case EXPERIMENT:
                return EMPTY_TEXT_WITH_KEY;
            case MATERIAL:
                return EMPTY_TEXT_WITH_KEY_AND_TYPE;
        }
        return null;
    }

}
