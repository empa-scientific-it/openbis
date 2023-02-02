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

package ch.systemsx.cisd.common.string;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * A little template engine. Usage example:
 * 
 * <pre>
 * Template template = new Template(&quot;Hello ${name}!&quot;);
 * template.bind(&quot;name&quot;, &quot;world&quot;);
 * String text = template.createText();
 * </pre>
 * 
 * The method {@link #bind(String, String)} throws an exception if the placeholder name is unknown. The method {@link #attemptToBind(String, String)}
 * returns <code>false</code> if the placeholder name is unknown. The method {@link #createText()} throws an exception if not all placeholders have
 * been bound.
 * <p>
 * Since placeholder bindings change the state of an instance of this class there is method {@link #createFreshCopy()} which creates a copy without
 * reparsing the template. Usage example:
 * 
 * <pre>
 * static final Template TEMPLATE = new Template(&quot;Hello ${name}!&quot;);
 * 
 * void doSomething()
 * {
 *     Template template = TEMPLATE.createFreshCopy();
 *     template.bind(&quot;name&quot;, &quot;world&quot;);
 *     String text = template.createText();
 * }
 * </pre>
 * 
 * @author Franz-Josef Elmer
 */
public class Template
{
    private static final char PLACEHOLDER_ESCAPE_CHARACTER = '$';

    private static final char PLACEHOLDER_START_CHARACTER = '{';

    private static final char PLACEHOLDER_METADATA_SEPARATOR = ':';

    private static final char PLACEHOLDER_END_CHARACTER = '}';

    private static final String createPlaceholder(String variableName)
    {
        return PLACEHOLDER_ESCAPE_CHARACTER + (PLACEHOLDER_START_CHARACTER + variableName)
                + PLACEHOLDER_END_CHARACTER;
    }

    public static interface IToken
    {
        /**
         * Append the token's value to the <var>builder</var>.
         */
        public void appendTo(StringBuilder builder);

        /**
         * Returns <code>true</code> if this token is a variable token, <code>false</code> otherwise.
         */
        public boolean isVariable();

        /**
         * Returns the name of the variable for a variable token, or <code>null</code> otherwise.
         */
        public String tryGetName();

        /**
         * Returns the index of the variable for a variable token, or -1 otherwise.
         */
        public int getVariableIndex();

        /**
         * Returns the value of the token or <code>null</code> if this is a variable token whose value has not yet been set.
         */
        public String tryGetValue();

        /**
         * Returns the metadata of the token for a variable token, or <code>null</code> otherwise.
         */
        public String tryGetMetadata();

        /**
         * Sets the value of the token.
         */
        public void setValue(String value);

        /**
         * Sets the value of the token to a substring given by <var>start</var> and <code>value.length() - indexFromEnd</code> and add
         * <var>prefix</var> and <var>suffix</var>.
         */
        public void setSubString(int start, int indexFromEnd, String prefix, String suffix);

        /**
         * Returns the prefix for a variable token, or <code>null</code> if no prefix has been set or it is not a variable token.
         */
        public String tryGetPrefix();

        /**
         * Returns the suffix for a variable token, or <code>null</code> if no prefix has been set or it is not a variable token.
         */
        public String tryGetSuffix();

        /**
         * Returns <code>false</code> for a variable token that is not yet bound, <code>true</code> otherwise.
         */
        public boolean isBound();

        /**
         * Binds the given <var>value</var> to a variable token.
         * 
         * @throw {@link UnsupportedOperationException} if this token is not a variable token.
         */
        public void bind(String value) throws UnsupportedOperationException;

    }

    public static final class PlainToken implements IToken
    {
        private String plainText;

        PlainToken(String plainText)
        {
            assert plainText != null : "Unspecified plain text.";
            this.plainText = plainText;
        }

        @Override
        public void appendTo(StringBuilder builder)
        {
            builder.append(plainText);
        }

        @Override
        public String tryGetName()
        {
            return null;
        }

        @Override
        public String tryGetMetadata()
        {
            return null;
        }

        @Override
        public String tryGetValue()
        {
            return plainText;
        }

        @Override
        public void setValue(String value)
        {
            this.plainText = value;
        }

        @Override
        public void setSubString(int start, int indexFromEnd, String prefix, String suffix)
        {
            this.plainText =
                    prefix + StringUtils.substring(plainText, start, StringUtils.length(plainText)
                            - indexFromEnd) + suffix;
        }

        @Override
        public String tryGetPrefix()
        {
            return null;
        }

        @Override
        public String tryGetSuffix()
        {
            return null;
        }

        @Override
        public boolean isVariable()
        {
            return false;
        }

        @Override
        public int getVariableIndex()
        {
            return -1;
        }

        @Override
        public boolean isBound()
        {
            return true;
        }

        @Override
        public void bind(String value) throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

    }

    public static final class VariableToken implements IToken
    {
        private final String variableName;

        private final String variableMetadata;

        private final int variableIndex;

        private String prefix;

        private String suffix;

        private String value;

        VariableToken(String variablePlaceHolder, String variableMetadata, int variableIndex)
        {
            assert variablePlaceHolder != null : "Unspecified variable place holder.";
            this.variableName = variablePlaceHolder;
            this.variableMetadata = StringUtils.isEmpty(variableMetadata) ? null : variableMetadata;
            this.variableIndex = variableIndex;
        }

        @Override
        public void appendTo(StringBuilder builder)
        {
            if (prefix != null)
            {
                builder.append(prefix);
            }
            builder.append(isBound() ? value : createPlaceholder(variableName));
            if (suffix != null)
            {
                builder.append(suffix);
            }
        }

        @Override
        public String tryGetName()
        {
            return variableName;
        }

        @Override
        public String tryGetMetadata()
        {
            return variableMetadata;
        }

        @Override
        public String tryGetValue()
        {
            return ((prefix != null) ? prefix : "") + value + ((suffix != null) ? suffix : "");
        }

        @Override
        public void setValue(String value)
        {
            bind(value);
        }

        @Override
        public void setSubString(int start, int indexFromEnd, String prefix, String suffix)
        {
            bind(StringUtils.substring(value, start, StringUtils.length(value) - indexFromEnd));
            this.prefix = prefix;
            this.suffix = suffix;
        }

        @Override
        public String tryGetPrefix()
        {
            return prefix;
        }

        @Override
        public String tryGetSuffix()
        {
            return suffix;
        }

        @Override
        public boolean isVariable()
        {
            return true;
        }

        @Override
        public boolean isBound()
        {
            return value != null;
        }

        @Override
        public void bind(String v)
        {
            this.value = v;
        }

        @Override
        public int getVariableIndex()
        {
            return variableIndex;
        }

    }

    private static enum State
    {
        PLAIN()
        {
            @Override
            State next(char character, TokenBuilder tokenBuilder)
            {
                if (character == PLACEHOLDER_ESCAPE_CHARACTER)
                {
                    return STARTING_PLACEHOLDER;
                }
                tokenBuilder.addCharacter(character);
                return PLAIN;
            }
        },

        STARTING_PLACEHOLDER()
        {
            @Override
            State next(char character, TokenBuilder tokenBuilder)
            {
                switch (character)
                {
                    case PLACEHOLDER_ESCAPE_CHARACTER:
                        tokenBuilder.addCharacter(PLACEHOLDER_ESCAPE_CHARACTER);
                        return PLAIN;
                    case PLACEHOLDER_START_CHARACTER:
                        tokenBuilder.finishPlainToken();
                        return PLACEHOLDER;
                    default:
                        tokenBuilder.addCharacter(PLACEHOLDER_ESCAPE_CHARACTER);
                        tokenBuilder.addCharacter(character);
                        return PLAIN;
                }
            }
        },

        PLACEHOLDER()
        {
            @Override
            State next(char character, TokenBuilder tokenBuilder)
            {
                if (character == PLACEHOLDER_END_CHARACTER)
                {
                    tokenBuilder.finishPlaceholder();
                    return PLAIN;
                } else if (character == PLACEHOLDER_METADATA_SEPARATOR)
                {
                    return STARTING_PLACEHOLDER_METADATA;
                }
                tokenBuilder.addCharacter(character);
                return PLACEHOLDER;
            }
        },

        STARTING_PLACEHOLDER_METADATA()
        {
            @Override
            State next(char character, TokenBuilder tokenBuilder)
            {
                if (character == PLACEHOLDER_METADATA_SEPARATOR)
                {
                    return PLACEHOLDER_METADATA;
                } else
                {
                    tokenBuilder.addCharacter(PLACEHOLDER_METADATA_SEPARATOR);
                    tokenBuilder.addCharacter(character);
                    return PLACEHOLDER;
                }
            }
        },

        PLACEHOLDER_METADATA()
        {
            @Override
            State next(char character, TokenBuilder tokenBuilder)
            {
                if (character == PLACEHOLDER_END_CHARACTER)
                {
                    tokenBuilder.finishPlaceholder();
                    return PLAIN;
                }
                tokenBuilder.addMetadataCharacter(character);
                return PLACEHOLDER_METADATA;
            }
        };

        abstract State next(char character, TokenBuilder tokenBuilder);
    }

    private static final class TokenBuilder
    {
        private final Map<String, VariableToken> variableTokens;

        private final List<IToken> tokens;

        private final StringBuilder builder;

        private final StringBuilder metadataBuilder;

        private int index;

        TokenBuilder(Map<String, VariableToken> variableTokens, List<IToken> tokens)
        {
            this.variableTokens = variableTokens;
            this.tokens = tokens;
            builder = new StringBuilder();
            metadataBuilder = new StringBuilder();
        }

        public void addCharacter(char character)
        {
            builder.append(character);
        }

        public void addMetadataCharacter(char character)
        {
            metadataBuilder.append(character);
        }

        public void finishPlainToken()
        {
            if (builder.length() > 0)
            {
                tokens.add(new PlainToken(builder.toString()));
                builder.setLength(0);
            }
        }

        public void finishPlaceholder()
        {
            String variableName = builder.toString();
            String variableMetadata = metadataBuilder.toString();
            if (variableName.length() == 0)
            {
                throw new IllegalArgumentException("Nameless placeholder " + createPlaceholder("")
                        + " found.");
            }
            VariableToken token = variableTokens.get(variableName);
            if (token == null)
            {
                token = new VariableToken(variableName, variableMetadata, index++);
                variableTokens.put(variableName, token);
            }
            tokens.add(token);
            builder.setLength(0);
            metadataBuilder.setLength(0);
        }
    }

    private final Map<String, VariableToken> variableTokens;

    private final List<IToken> tokens;

    /**
     * Creates a new instance for the specified template.
     * 
     * @throws IllegalArgumentException if some error occurred during parsing.
     */
    public Template(String template)
    {
        this(new LinkedHashMap<String, VariableToken>(), new ArrayList<IToken>());
        assert template != null : "Unspecified template.";

        TokenBuilder tokenBuilder = new TokenBuilder(variableTokens, tokens);
        State state = State.PLAIN;
        final int n = template.length();
        for (int i = 0; i < n; i++)
        {
            state = state.next(template.charAt(i), tokenBuilder);
        }
        if (state != State.PLAIN)
        {
            throw new IllegalArgumentException("Incomplete placeholder detected at the end.");
        }
        tokenBuilder.finishPlainToken();
    }

    private Template(Map<String, VariableToken> variableTokens, List<IToken> tokens)
    {
        this.variableTokens = variableTokens;
        this.tokens = tokens;
    }

    /**
     * Creates a copy of this template with no variable bindings.
     */
    public Template createFreshCopy()
    {
        LinkedHashMap<String, VariableToken> map = new LinkedHashMap<String, VariableToken>();
        for (VariableToken variableToken : variableTokens.values())
        {
            final String variableName = variableToken.tryGetName();
            final String variableMetadata = variableToken.tryGetMetadata();
            final int variableIndex = variableToken.getVariableIndex();
            map.put(variableName, new VariableToken(variableName, variableMetadata, variableIndex));
        }
        ArrayList<IToken> list = new ArrayList<IToken>();
        for (IToken token : tokens)
        {
            if (token instanceof VariableToken)
            {
                list.add(map.get(((VariableToken) token).tryGetName()));
            } else
            {
                list.add(token);
            }
        }
        return new Template(map, list);
    }

    /**
     * Returns all placeholder names.
     */
    public Set<String> getPlaceholderNames()
    {
        return variableTokens.keySet();
    }

    /**
     * Returns all tokens of this template.
     */
    public List<IToken> getTokens()
    {
        return tokens;
    }

    /**
     * Binds the specified value to the specified placeholder name.
     * 
     * @throws IllegalArgumentException if placeholder is not known.
     */
    public void bind(String placeholderName, String value)
    {
        boolean successful = attemptToBind(placeholderName, value);
        if (successful == false)
        {
            throw new IllegalArgumentException("Unknown variable '" + placeholderName + "'.");
        }
    }

    /**
     * Attempts to bind the specified value to the specified placeholder name.
     * 
     * @return <code>true</code> if successful.
     */
    public boolean attemptToBind(String placeholderName, String value)
    {
        assert placeholderName != null : "Unspecified placeholder name.";
        assert value != null : "Unspecified value for '" + placeholderName + "'";

        VariableToken variableToken = variableTokens.get(placeholderName);
        if (variableToken == null)
        {
            return false;
        }
        variableToken.bind(value);
        return true;
    }

    /**
     * Returns index (position, starting with 0) of the <var>placeholderName</var>, or -1, if the place holder name cannot be found in the template.
     */
    public int tryGetIndex(String placeholderName)
    {
        assert placeholderName != null : "Unspecified placeholder name.";
        VariableToken variableToken = variableTokens.get(placeholderName);
        if (variableToken == null)
        {
            return -1;
        }
        return variableToken.getVariableIndex();
    }

    /**
     * Returns the metadata of the given <var>placeholderName</var>, or <code>null</code>, if the place holder name cannot be found in the template or
     * there are no metadata for this placeholder.
     */
    public String tryGetMetadata(String placeholderName)
    {
        assert placeholderName != null : "Unspecified placeholder name.";
        VariableToken variableToken = variableTokens.get(placeholderName);
        if (variableToken == null)
        {
            return null;
        }
        return variableToken.tryGetMetadata();
    }

    /**
     * Look through all tokens for variable tokens which are surrounded by plain text tokens which end / start with the given
     * <var>oldOpeningBracket</var> / <var>oldClosingBracket</var>. If that is the case, replace these brackets with <var>newOpeningBracket</var> /
     * <var>newClosingBracket</var>.
     * 
     * @return The variable tokens where brackets have been replaced.
     */
    public List<IToken> replaceBrackets(String oldOpeningBracket, String oldClosingBracket,
            String newOpeningBracket, String newClosingBracket)
    {
        final List<IToken> tokensReplaced = new ArrayList<IToken>();
        for (int i = 1; i < tokens.size() - 1; ++i)
        {
            if (tokens.get(i).isVariable() && tokenEndsWith(i - 1, oldOpeningBracket)
                    && tokenStartsWith(i + 1, oldClosingBracket))
            {
                tokens.get(i - 1).setSubString(0, oldOpeningBracket.length(), "",
                        newOpeningBracket);
                tokens.get(i + 1).setSubString(oldClosingBracket.length(), 0,
                        newClosingBracket, "");
                tokensReplaced.add(tokens.get(i));
            }
        }
        return tokensReplaced;
    }

    /**
     * Returns the left neighbor token of the variable token given by <var>variableName</var>, or <code>null</code>, if this variable token is not
     * found or doesn't have a left neighbor.
     */
    public IToken tryGetLeftNeighbor(String variableName)
    {
        for (int i = 1; i < tokens.size() - 1; ++i)
        {
            if (tokens.get(i).isVariable()
                    && StringUtils.equals(tokens.get(i).tryGetName(), variableName))
            {
                return tokens.get(i - 1);
            }
        }
        return null;
    }

    /**
     * Returns the right neighbor token of the variable token given by <var>variableName</var>, or <code>null</code>, if this variable token is not
     * found or doesn't have a right neighbor.
     */
    public IToken tryGetRightNeighbor(String variableName)
    {
        for (int i = 1; i < tokens.size() - 1; ++i)
        {
            if (tokens.get(i).isVariable()
                    && StringUtils.equals(tokens.get(i).tryGetName(), variableName))
            {
                return tokens.get(i + 1);
            }
        }
        return null;
    }

    private boolean tokenStartsWith(int tokenInx, String text)
    {
        final IToken token = tokens.get(tokenInx);
        return (token.isVariable() == false && StringUtils.startsWith(token.tryGetValue(), text));
    }

    private boolean tokenEndsWith(int tokenInx, String text)
    {
        final IToken token = tokens.get(tokenInx);
        return (token.isVariable() == false && StringUtils.endsWith(token.tryGetValue(), text));
    }

    /**
     * Creates the text by using all placeholder bindings.
     * 
     * @throws IllegalStateException if not all placeholders have been bound.
     */
    public String createText()
    {
        return createText(true);
    }

    /**
     * Creates the text by using placeholder bindings.
     * 
     * @param complete If <code>true</code> an {@link IllegalStateException} will be thrown if not all bindings are set.
     */
    public String createText(boolean complete)
    {
        if (complete)
        {
            assertAllVariablesAreBound();
        }
        StringBuilder builder = new StringBuilder();
        for (IToken token : tokens)
        {
            token.appendTo(builder);
        }
        return builder.toString();
    }

    private void assertAllVariablesAreBound()
    {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, VariableToken> entry : variableTokens.entrySet())
        {
            if (entry.getValue().isBound() == false)
            {
                builder.append(entry.getKey()).append(' ');
            }
        }
        if (builder.length() > 0)
        {
            throw new IllegalStateException("The following variables are not bound: " + builder);
        }
    }

    /**
     * Returns <code>true</code> if all variables are bound, <code>false</code> otherwise.
     */
    public boolean allVariablesAreBound()
    {
        for (VariableToken value : variableTokens.values())
        {
            if (value.isBound() == false)
            {
                return false;
            }
        }
        return true;
    }
}
