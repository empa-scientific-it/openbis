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

package ch.systemsx.cisd.common.db;

import java.sql.Timestamp;
import java.util.Date;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * Useful utility method concerning database code.
 * 
 * @author Basil Neff
 * @author Pawel Glyzewski
 */
public final class DBUtils
{
    /**
     * Translates the specified timestamp to a {@link Date} object.
     * 
     * @return <code>null</code> if <code>timestamp == null</code>.
     */
    public final static Date tryToTranslateTimestampToDate(final Timestamp timestampOrNull)
    {
        return timestampOrNull == null ? null : new Date(timestampOrNull.getTime());
    }

    /**
     * Translates given regular expression to database-like pattern if possible. Returns <code>null</code> if translation is not possible.
     * 
     * @return regular expression in database-like pattern, or <code>null</code> if translation is not possible.
     */
    public static String tryToTranslateRegExpToLikePattern(String regexpOrNull)
    {
        if (StringUtils.isBlank(regexpOrNull))
        {
            return null;
        }

        StringBuilder result = new StringBuilder();

        int startPosition = 0;
        if (regexpOrNull.startsWith("^"))
        {
            startPosition++;
        } else
        {
            result.append('%');
        }

        while (startPosition < regexpOrNull.length())
        {
            char ch = regexpOrNull.charAt(startPosition);
            if (Character.isLetter(ch) || Character.isDigit(ch) || Character.isWhitespace(ch))
            { // just copy letters, digits and whitespaces
                result.append(ch);
                startPosition++;
            } else
            {
                switch (ch)
                {
                    case '/': // standard characters which should be simply copied to result
                    case ',':
                    case '-':
                    case '#':
                    case '@':
                    case '&':
                    case '\'':
                    case '"':
                    case ':':
                    case ';':
                    case '`':
                    case '~':
                    case '=':
                        result.append(ch);
                        startPosition++;
                        break;
                    case '%': // special characters for db-like form, should be escaped
                    case '_':
                        result.append('\\').append(ch);
                        startPosition++;
                        break;
                    case '.': // translates . to _, .* to % and .+ to _%
                        startPosition++;
                        if (startPosition < regexpOrNull.length()
                                && regexpOrNull.charAt(startPosition) == '*')
                        {
                            result.append('%');
                            startPosition++;
                        } else if (startPosition < regexpOrNull.length()
                                && regexpOrNull.charAt(startPosition) == '+')
                        {
                            result.append('_').append('%');
                            startPosition++;
                        } else
                        {
                            result.append('_');
                        }
                        break;
                    case '$': // end of string
                        startPosition++;
                        if (startPosition < regexpOrNull.length())
                        {
                            result.append(ch);
                        }
                        break;
                    case '\\': // unescape characters
                        startPosition++;
                        if (startPosition < regexpOrNull.length())
                        {
                            char escaped = regexpOrNull.charAt(startPosition);
                            switch (escaped)
                            {
                                case '\\':
                                    startPosition++;
                                    result.append('\\').append('\\');
                                    break;
                                case '.':
                                case '$':
                                case '^':
                                case '(':
                                case ')':
                                case '[':
                                case ']':
                                case '?':
                                case '*':
                                case '{':
                                case '}':
                                case '|':
                                case '+':
                                case '-':
                                case '#':
                                case '@':
                                case '&':
                                case '\'':
                                case '"':
                                case ':':
                                case ';':
                                case '`':
                                case '~':
                                case '=':
                                    startPosition++;
                                    result.append(escaped);
                                    break;
                                case '%':
                                case '_':
                                    startPosition++;
                                    result.append('\\').append(escaped);
                                    break;
                                default: // unsupported character
                                    return null;
                            }
                        } else
                        // unsupported character
                        {
                            return null;
                        }
                        break;
                    default: // unsupported character
                        return null;
                }
            }
        }

        // check if we should match end of patters in exact way
        if (false == regexpOrNull.endsWith("$"))
        {
            result.append('%');
        }
        return result.toString();
    }

    private DBUtils()
    {
    }
}
