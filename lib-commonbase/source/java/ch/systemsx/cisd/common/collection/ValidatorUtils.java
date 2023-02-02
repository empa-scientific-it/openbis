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

package ch.systemsx.cisd.common.collection;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.systemsx.cisd.common.collection.CompositeValidator;
import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.common.collection.RegExValidator;

/**
 * <code>ValidatorUtils</code> provides reference implementations and utilities for the <code>IValidator</code> interface.
 * 
 * @author Christian Ribeaud
 */
public final class ValidatorUtils
{

    /**
     * A <code>IValidator</code> implementation which check whether given <code>Object</code> is not <code>null</code>.
     */
    private final static IValidator<Object> NOT_NULL_VALIDATOR = new IValidator<Object>()
        {

            //
            // IValidator
            //

            @Override
            public final boolean isValid(final Object object)
            {
                return object != null;
            }
        };

    /**
     * A <code>IValidator</code> implementation which always returns <code>true</code>.
     */
    private final static IValidator<Object> ALWAYS_TRUE = new IValidator<Object>()
        {

            //
            // IValidator
            //

            @Override
            public final boolean isValid(final Object object)
            {
                return true;
            }
        };

    private ValidatorUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Creates a <code>IValidator</code> based on the given pattern.
     * 
     * @return <code>null</code> if given <var>pattern</var> is also <code>null</code>.
     * @throws PatternSyntaxException if the expression's syntax is invalid.
     */
    public final static IValidator<String> createCaseInsensitivePatternValidator(
            final String[] patterns)
    {
        assert patterns != null : "Unspecified patterns.";
        final int length = patterns.length;
        switch (length)
        {
            case 0:
                return null;
            case 1:
                return createCaseInsensitivePatternValidator(patterns[0]);
            default:
                final CompositeValidator<String> validator = new CompositeValidator<String>();
                for (final String pattern : patterns)
                {
                    validator.addValidator(createCaseInsensitivePatternValidator(pattern));
                }
                return validator;
        }
    }

    /**
     * Creates a case insensitive <code>IValidator</code> based on the given pattern.
     * 
     * @return <code>null</code> if given <var>pattern</var> is also <code>null</code>.
     * @throws PatternSyntaxException if the expression's syntax is invalid.
     */
    public final static IValidator<String> createCaseInsensitivePatternValidator(
            final String pattern)
    {
        if (pattern == null)
        {
            return null;
        }
        final Pattern regEx = Pattern.compile(convertToRegEx(pattern), Pattern.CASE_INSENSITIVE);
        return new RegExValidator(regEx);
    }

    /** Returns a typed validator for non-<code>null</code> objects. */
    @SuppressWarnings(
    { "unchecked" })
    public static final <T> IValidator<T> getNotNullValidator()
    {
        return (IValidator<T>) NOT_NULL_VALIDATOR;
    }

    /** Returns a typed validator which always returns <code>true</code>. */
    @SuppressWarnings(
    { "unchecked" })
    public static final <T> IValidator<T> getAlwaysTrueValidator()
    {
        return (IValidator<T>) ALWAYS_TRUE;
    }

    /**
     * Converts given pattern into a regular expression. This method does the following:
     * <ol>
     * <li>replaces any <code>?</code> with <code>.</code></li>
     * <li>replaces any <code>*</code> with <code>.*</code></li>
     * </ol>
     */
    final static String convertToRegEx(final String pattern)
    {
        assert pattern != null;
        final StringBuilder stringBuilder = new StringBuilder();
        final char[] chars = pattern.toCharArray();
        boolean escape = false;
        for (final char c : chars)
        {
            String toAppend;
            if (c == '\\')
            {
                escape = true;
                toAppend = c + "";
            } else if (c == '?' || c == '*')
            {
                if (escape == false)
                {
                    toAppend = ".";
                    if (c == '*')
                    {
                        toAppend += "*";
                    }
                } else
                {
                    toAppend = c + "";
                    escape = false;
                }
            } else
            {
                toAppend = c + "";
                escape = false;
            }
            stringBuilder.append(toAppend);
        }
        return stringBuilder.toString();
    }
}
