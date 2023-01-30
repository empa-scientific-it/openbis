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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.widget.form.TriggerField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * The {@link VarcharField} extension suitable for registering a mandatory code.
 * 
 * @author Christian Ribeaud
 */
public class CodeField extends TriggerField<String>
{
    private static final int CODE_MAX_LENGTH = 100;

    private static final String CODE_CHAR_PATTERN = "a-zA-Z0-9_\\-\\.";

    public static final String CODE_CHARS = "[" + CODE_CHAR_PATTERN + "]+";

    public final static String CODE_PATTERN = "^" + CODE_CHARS + "$";

    private final static String CODE_PATTERN_ALLOWED_CHARACTERS =
            "letters, numbers, \"-\", \"_\", " + "\".\"";

    public final static String CODE_PATTERN_WITH_COLON = "^[" + CODE_CHAR_PATTERN + ":]+$";

    private final static String CODE_PATTERN_WITH_COLON_ALLOWED_CHARACTERS =
            CODE_PATTERN_ALLOWED_CHARACTERS + ", \":\"";

    public final static String CODE_OR_EMAIL_PATTERN = "^[" + CODE_CHAR_PATTERN + "@]+$";

    private final static String CODE_OR_EMAIL_PATTERN_ALLOWED_CHARACTERS =
            CODE_PATTERN_ALLOWED_CHARACTERS + "@";

    public enum CodeFieldKind
    {
        /**
         * Field with a basic code pattern (letters, numbers, '-', '_', '.').
         * <p>
         * Note that this one accepts also letters in lower case.
         * </p>
         */
        BASIC_CODE(CODE_PATTERN, CODE_PATTERN_ALLOWED_CHARACTERS),

        /**
         * Field with a basic code pattern which accepts also email addresses.
         */
        CODE_OR_EMAIL(CODE_OR_EMAIL_PATTERN, CODE_OR_EMAIL_PATTERN_ALLOWED_CHARACTERS),

        /**
         * Field with a basic code pattern extended by ':'.
         * <p>
         * Useful for user vocabulary term codes.
         * </p>
         */
        CODE_WITH_COLON(CODE_PATTERN_WITH_COLON, CODE_PATTERN_WITH_COLON_ALLOWED_CHARACTERS);

        private final String pattern;

        private final String allowedCharacters;

        CodeFieldKind(String pattern, String allowedCharacters)
        {
            this.pattern = pattern;
            this.allowedCharacters = allowedCharacters;
        }

        public String getPattern()
        {
            return pattern;
        }

        public String getAllowedCharacters()
        {
            return allowedCharacters;
        }
    }

    /** Constructor of a code field with {@link CodeFieldKind#BASIC_CODE} kind. */
    public CodeField(final IMessageProvider messageProvider, final String label)
    {
        this(messageProvider, label, CodeFieldKind.BASIC_CODE);
    }

    public CodeField(final IMessageProvider messageProvider, final String label,
            final CodeFieldKind kind)
    {
        this(messageProvider, label, kind.getPattern(), kind.getAllowedCharacters());
    }

    private CodeField(final IMessageProvider messageProvider, final String label,
            final String pattern, final String allowedCharacters)
    {
        VarcharField.configureField(this, label, true);
        setMaxLength(CODE_MAX_LENGTH);
        setRegex(pattern);
        getMessages().setRegexText(
                messageProvider.getMessage(Dict.INVALID_CODE_MESSAGE, allowedCharacters));
        setHideTrigger(true);
    }

}
