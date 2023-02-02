/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.parser;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;

/**
 * A default <code>ILineTokenizer</code> implementation that parses a line into an array of <code>String</code> objects. This implementation uses
 * {@link StrTokenizer} as internal worker.
 * <p>
 * The default separator chars used here are <code>\t</code>. If you want to change that, use {@link #setProperty(PropertyKey, String)} with
 * corresponding property key defined here.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class DefaultLineTokenizer implements ILineTokenizer<String>
{

    /** Allowed <code>Properties</code> keys. */
    public static enum PropertyKey
    {
        /** The property key for setting some delimiter characters. */
        SEPARATOR_CHARS,
        /** The property key for setting some quote characters. */
        QUOTE_CHARS,
        /** The property key for setting some ignored characters. */
        IGNORED_CHARS,
        /** The property key for setting some trimmer characters. */
        TRIMMER_CHARS,
    }

    /** Some properties for this tab parser. */
    private final Map<PropertyKey, String> properties;

    /** The default <code>StrMatcher</code> for each <code>PropertyKey</code>. */
    private final static Map<PropertyKey, StrMatcher> defaultStrMatchers =
            createDefaultStrMatchers();

    /**
     * Original value is <code>null</code>.
     * <p>
     * If not <code>null</code> then we assume that {@link #init()} method has been called.
     * </p>
     */
    private StrTokenizer tokenizer;

    public DefaultLineTokenizer()
    {
        this.properties = new EnumMap<PropertyKey, String>(PropertyKey.class);
    }

    private static final Map<PropertyKey, StrMatcher> createDefaultStrMatchers()
    {
        EnumMap<PropertyKey, StrMatcher> map =
                new EnumMap<PropertyKey, StrMatcher>(PropertyKey.class);
        map.put(PropertyKey.SEPARATOR_CHARS, StrMatcher.tabMatcher());
        map.put(PropertyKey.QUOTE_CHARS, StrMatcher.noneMatcher());
        map.put(PropertyKey.TRIMMER_CHARS, StrMatcher.trimMatcher());
        map.put(PropertyKey.IGNORED_CHARS, StrMatcher.noneMatcher());
        return map;
    }

    /**
     * Sets a property for this <code>TabReaderParser</code>.
     * <p>
     * Does nothing if given <code>key</code> is <code>null</code> and resets <code>key</code> to default value if given <code>value</code> is
     * <code>null</code>.
     * </p>
     */
    public final void setProperty(PropertyKey key, String value)
    {
        if (key == null)
        {
            return;
        }
        if (value == null)
        {
            properties.remove(key);
        }
        properties.put(key, value);
        if (tokenizer != null)
        {
            StrMatcher matcher = getStrMatcher(key);
            if (key == PropertyKey.SEPARATOR_CHARS)
            {
                tokenizer.setDelimiterMatcher(matcher);
            } else if (key == PropertyKey.QUOTE_CHARS)
            {
                tokenizer.setQuoteMatcher(matcher);
            } else if (key == PropertyKey.TRIMMER_CHARS)
            {
                tokenizer.setTrimmerMatcher(matcher);
            } else if (key == PropertyKey.IGNORED_CHARS)
            {
                tokenizer.setIgnoredMatcher(matcher);
            }
        }
    }

    /** Converts a defined <code>PropertyKey</code> into <code>StrMatcher</code>. */
    private final StrMatcher getStrMatcher(PropertyKey key)
    {
        StrMatcher strMatcher = defaultStrMatchers.get(key);
        String value = properties.get(key);
        if (value != null)
        {
            // Note that we use a set of characters (like <code>StringTokenizer</code>) does
            // and not <code>StrMatcher.stringMatcher(value)</code>
            strMatcher = StrMatcher.charSetMatcher(value);
        }
        return strMatcher;
    }

    //
    // ILineTokenizer
    //

    @Override
    public final void init()
    {
        StrTokenizer strTokenizer = new StrTokenizer();
        strTokenizer.setDelimiterMatcher(getStrMatcher(PropertyKey.SEPARATOR_CHARS));
        strTokenizer.setQuoteMatcher(getStrMatcher(PropertyKey.QUOTE_CHARS));
        strTokenizer.setTrimmerMatcher(getStrMatcher(PropertyKey.TRIMMER_CHARS));
        strTokenizer.setIgnoredMatcher(getStrMatcher(PropertyKey.IGNORED_CHARS));
        strTokenizer.setEmptyTokenAsNull(false);
        strTokenizer.setIgnoreEmptyTokens(false);
        this.tokenizer = strTokenizer;
    }

    @Override
    public final String[] tokenize(String line)
    {
        return tokenizer.reset(line).getTokenArray();
    }

    @Override
    public final void destroy()
    {
        tokenizer = null;
    }
}