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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier.Constants;

/**
 * A general parser to get the database instance code and the group code out of the given string. The expected format is the following:
 * 
 * <pre>
 * [&lt;database-instance-code&gt;:][/&lt;space-code&gt;/]
 * </pre>
 * 
 * @author Basil Neff
 * @author Tomasz Pylak
 */
public abstract class AbstractIdentifierFactory
{
    public static final String NO_GROUP_PREFIX =
            String.format("space identifier does not start with '%s'",
                    Constants.IDENTIFIER_SEPARATOR);

    public final static String ILLEGAL_EMPTY_IDENTIFIER = "Illegal empty identifier";

    final static String ILLEGAL_IDENTIFIER_TEMPLATE = "Illegal identifier '%s': %s.";

    public final static String ILLEGAL_CHARACTERS = "contains illegal characters.";

    final static String ILLEGAL_CODE_CHARACTERS_TEMPLATE = "The code '%s' " + ILLEGAL_CHARACTERS;

    final static String ILLEGAL_PATTERN_CHARACTERS_TEMPLATE =
            "The pattern '%s' " + ILLEGAL_CHARACTERS;

    final static String IDENTIFIER_IS_INCOMPLETE = "identifier is incomplete";

    public static final String TOO_MANY_TOKENS = "too many tokens found";

    // ----

    private static final String ALLOWED_CODE_CHARACTERS = "A-Z0-9_\\-\\.";

    private static final Pattern ALLOWED_CODE_REGEXP =
            Pattern.compile("^[" + ALLOWED_CODE_CHARACTERS + "]+$", Pattern.CASE_INSENSITIVE);

    private static final Pattern ALLOWED_PATTERN_REGEXP =
            Pattern
                    .compile("^[" + ALLOWED_CODE_CHARACTERS + "?*" + "]+$",
                            Pattern.CASE_INSENSITIVE);

    protected static final String IDENTIFIER_SEPARARTOR_STRING =
            Constants.IDENTIFIER_SEPARATOR + "";

    private final String textToParse;

    protected AbstractIdentifierFactory(final String textToParse) throws UserFailureException
    {
        assert textToParse != null : "Unspecified text to parse";
        this.textToParse = textToParse;
    }

    protected final static void assertValidPatternCharacters(final String textOrNull)
            throws UserFailureException
    {
        assertValidCharacters(textOrNull, ALLOWED_PATTERN_REGEXP,
                ILLEGAL_PATTERN_CHARACTERS_TEMPLATE);
    }

    public final static String assertValidCode(final String textOrNull)
            throws UserFailureException
    {
        assertValidCharacters(textOrNull, ALLOWED_CODE_REGEXP, ILLEGAL_CODE_CHARACTERS_TEMPLATE);
        return textOrNull;
    }

    protected final static void assertValidCharacters(final String textOrNull,
            final Pattern pattern, final String invalidPatternMsg) throws UserFailureException
    {
        if (textOrNull == null)
        {
            return;
        }
        assertNotEmpty(textOrNull);
        if (pattern.matcher(textOrNull).matches() == false)
        {
            throw UserFailureException.fromTemplate(invalidPatternMsg, textOrNull);
        }
    }

    private final static void assertNotEmpty(final String text) throws UserFailureException
    {
        if (StringUtils.isEmpty(text))
        {
            throw new UserFailureException(ILLEGAL_EMPTY_IDENTIFIER);
        }
    }

    // NOTE: if there is "/" at the beginning of the text, first token is an empty string
    protected static class TokenLexer
    {
        private final String originalText;

        private int nextIx = 0;

        private final String[] tokens;

        public TokenLexer(String text)
        {
            assertNotEmpty(text);
            this.tokens = text.split(IDENTIFIER_SEPARARTOR_STRING);
            this.originalText = text;
            if (tokens.length == 0)
            {
                throw new UserFailureException(ILLEGAL_EMPTY_IDENTIFIER);
            }
        }

        public void ensureNoTokensLeft()
        {
            if (hasNext())
            {
                throw createTooManyTokensExcp(originalText);
            }
        }

        // returns a current token
        public String peek()
        {
            return tokens[nextIx];
        }

        public boolean hasNext()
        {
            return nextIx < tokens.length;
        }

        // returns a current token and moves to the next one
        public String next()
        {
            if (nextIx >= tokens.length)
            {
                throw UserFailureException.fromTemplate(ILLEGAL_IDENTIFIER_TEMPLATE, originalText,
                        IDENTIFIER_IS_INCOMPLETE);
            }
            return tokens[nextIx++];
        }

        public String getOriginalText()
        {
            return originalText;
        }
    }

    protected static UserFailureException createTooManyTokensExcp(String text)
    {
        return UserFailureException
                .fromTemplate(ILLEGAL_IDENTIFIER_TEMPLATE, text, TOO_MANY_TOKENS);
    }

    protected static UserFailureException createSlashMissingExcp(String text)
    {
        return UserFailureException
                .fromTemplate(ILLEGAL_IDENTIFIER_TEMPLATE, text, NO_GROUP_PREFIX);
    }

    /**
     * Returns the original text that has been parsed.
     */
    protected final String getTextToParse()
    {
        return textToParse;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }
}
