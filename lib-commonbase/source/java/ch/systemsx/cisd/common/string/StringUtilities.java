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

package ch.systemsx.cisd.common.string;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.utilities.OSUtilities;

/**
 * Some useful utlities methods for {@link String}s.
 * <p>
 * If you are tempted to add new functionality to this class, ensure that the new functionality does not yet exist in {@link StringUtils}, see <a
 * href= "http://jakarta.apache.org/commons/lang/api-release/org/apache/commons/lang/StringUtils.html" >javadoc</a>.
 * 
 * @author Bernd Rinn
 */
public final class StringUtilities
{

    private static final String[] STRINGS =
            new String[]
            { "phalanx", "nightmare", "concierge", "asbestos", "cody", "hermit", "nbc",
                    "couplet", "dice", "thumbnail", "finley", "figure", "exclamation",
                    "whoosh", "punish", "servitor", "portend", "boulevard", "bacterial",
                    "dilate", "emboss", "birmingham", "illustrate", "pomona", "truk",
                    "bursitis", "trustworthy", "harriman", "schenectady", "obligate",
                    "oceania", "knew", "quickstep", "woo", "strickland", "sadie", "malabar",
                    "posit", "breadfruit", "grandfather", "vishnu", "vacuous", "melpomene",
                    "assam", "blaine", "taskmaster", "polymeric", "hector",
                    "counterrevolution", "compassionate", "linkage", "distant", "vet", "shako",
                    "eagan", "neutronium", "stony", "lie", "hoydenish", "dial", "hecate",
                    "pinch", "olin", "piglet", "basswood", "yawn", "ouzo", "scrupulosity",
                    "bestiary", "subpoena", "nudge", "baton", "thing", "hallmark", "bossy",
                    "preferential", "bambi", "narwhal", "brighten", "omnipotent", "forsake",
                    "flapping", "orthodoxy", "upcome", "teaspoonful", "wabash", "lipid",
                    "enjoin", "shoshone", "wartime", "gatekeeper", "litigate", "siderite",
                    "sadden", "visage", "boogie", "scald", "equate", "tragic", "ordinary",
                    "wick", "gigawatt", "desultory", "bambi", "aureomycin", "car", "especial",
                    "rescue", "protector", "burnett", "constant", "heroes", "filmstrip",
                    "homeown", "verdant", "governor", "cornwall", "predisposition", "sedan",
                    "resemblant", "satellite", "committeemen", "given", "narragansett",
                    "switzer", "clockwatcher", "sweeten", "monologist", "execrate", "gila",
                    "lad", "mahayanist", "solicitation", "linemen", "reading", "hoard",
                    "phyla", "carcinoma", "glycol", "polymer", "hangmen", "dualism",
                    "betrayal", "corpsman", "stint", "hannah", "balsam", "granola",
                    "charitable", "osborn", "party", "laboratory", "norwich", "laxative",
                    "collude", "rockefeller", "crack", "lamarck", "purposeful", "neuroanotomy",
                    "araby", "crucible", "oratorical", "dramaturgy", "kitty", "pit", "ephesus",
                    "bum", "amuse", "clogging", "joker", "fobbing", "extent", "colossal",
                    "macromolecule", "choppy", "tennessee", "primrose", "glassine", "vampire",
                    "chap", "precursor", "incorrigible", "slither", "interrogate", "spectral",
                    "debut", "creche", "pyrolysis", "homicidal", "sonnet", "gin", "science",
                    "magma", "metaphor", "cobble", "dyer", "narrate", "goody", "optometric" };

    private final static Pattern matrixSplitPattern = Pattern.compile("([a-zA-Z]+)([0-9]+)");

    private StringUtilities()
    {
        // This class can not be instantiated.
    }

    /**
     * Computes the MD5 hash value of <var>string</var>.
     * 
     * @return 32-character hexadecimal representation of the calculated value.
     */
    public static String computeMD5Hash(String string)
    {
        assert string != null : "Unspecified string.";

        try
        {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(string.getBytes("utf8"));
            byte messageDigest[] = algorithm.digest();

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++)
            {
                byte b = messageDigest[i];
                builder.append(Integer.toHexString(0xF & (b >> 4)));
                builder.append(Integer.toHexString(0xF & b));
            }
            return builder.toString();
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * <em>Note that this method will transform <code>cAptcha</code> into <code>Captcha</code>, while 
     * {@link StringUtils#capitalize(String)} would transform it into <code>CAptcha</code>.</em>
     * 
     * @return The capitalized form of <var>string</var>.
     */
    public static String capitalize(String string)
    {
        if (StringUtils.isBlank(string))
        {
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    /**
     * @return The concatenated entries of the <var>list</var>, delimited by a space.
     */
    public static String concatenateWithSpace(List<String> list)
    {
        return StringUtils.join(list, " ");
    }

    /**
     * @return The concatenated entries of the <var>list</var>, delimited by a new line.
     */
    public static String concatenateWithNewLine(List<String> list)
    {
        return StringUtils.join(list, OSUtilities.LINE_SEPARATOR);
    }

    /**
     * Returns an array of as many strings as you request.
     */
    public static final String[] getStrings(int numberOfStrings)
    {
        String[] result = new String[numberOfStrings];
        for (int i = 0; i < numberOfStrings; i++)
        {
            result[i] = getString();
        }
        return result;
    }

    /** Returns a random string. */
    public static final String getString()
    {
        return STRINGS[(int) (Math.random() * STRINGS.length - 1)];
    }

    /**
     * Splits a matrix coordinate and returns the result in a two-dimensional array.
     * <p>
     * For instance the following matrix coordinate <code>H24</code> will returns the following array <code>[H, 24]</code>.
     * </p>
     * 
     * @return <code>null</code> if the operation fails.
     */
    public final static String[] splitMatrixCoordinate(final String text)
    {
        assert text != null : "Given text can not be null.";
        final Matcher matcher = matrixSplitPattern.matcher(text);
        if (matcher.matches())
        {
            return new String[]
            { matcher.group(1), matcher.group(2) };
        }
        return null;
    }

    /**
     * Returns the ordinal representation of given <var>number</var>.
     * 
     * @param number must be <code>&gt;= 0</code>.
     * @return never <code>null</code>.
     */
    public final static String getOrdinal(final int number)
    {
        assert number >= 0 : "Given number must be >= 0.";
        int modulo = number % 100;
        if (modulo < 11 || modulo > 13)
        {
            modulo = number % 10;
            switch (modulo)
            {
                case 1:
                    return number + "st";
                case 2:
                    return number + "nd";
                case 3:
                    return number + "rd";
                default:
                    // No special treatment needed, "th" appended below.
            }
        }
        return number + "th";
    }

    /** compares two strings which can be null. Null is smaller than not-null string. */
    public static int compareNullable(String s1OrNull, String s2OrNull)
    {
        if (s1OrNull == null)
        {
            return s2OrNull == null ? 0 : -1;
        } else if (s2OrNull == null)
        {
            return 1;
        } else
        {
            return s1OrNull.compareTo(s2OrNull);
        }
    }

    /**
     * Returns a list of tokens of the specified string which are separated by at least one whitespace character or comma symbol.
     */
    public static List<String> tokenize(String textOrNull)
    {
        List<String> list = new ArrayList<String>();
        if (textOrNull != null)
        {
            final StringTokenizer stringTokenizer = new StringTokenizer(textOrNull, ", \t\n\r\f");
            while (stringTokenizer.hasMoreTokens())
            {
                list.add(stringTokenizer.nextToken());
            }
        }
        return list;
    }

    /** A regular expression pattern matching one or more digits. */
    private final static Pattern ONE_OR_MORE_DIGITS = Pattern.compile(".*(\\d+)$");

    public interface IUniquenessChecker
    {
        /**
         * Returns <code>true</code> if <var>str</var> is unique, <var>false</var> otherwise.
         */
        boolean isUnique(String str);
    }

    /**
     * Creates the next numbered string if given <var>str</var> is not unique.
     * <p>
     * If the new suggested string already exists, then this method is called recursively.
     */
    public final static String createUniqueString(final String str, final IUniquenessChecker checker)
    {
        return createUniqueString(str, checker, null, null);
    }

    /**
     * Creates the next numbered string if given <var>str</var> is not unique.
     * <p>
     * If the new suggested string already exists, then this method is called recursively.
     * </p>
     * 
     * @param defaultStrOrNull the default value for the new string if the digit pattern could not be found in <var>str</var>. If <code>null</code>
     *            then "1" will be appended to <var>str</var>.
     * @param regexOrNull pattern to find out the counter. If <code>null</code> then a default ( <code>(\\d+)</code>) will be used. The given
     *            <var>regex</var> must contain <code>(\\d+)</code> or <code>([0-9]+)</code>.
     */
    public final static String createUniqueString(final String str,
            final IUniquenessChecker checker, final Pattern regexOrNull,
            final String defaultStrOrNull)
    {
        assert str != null;
        assert checker != null;

        if (checker.isUnique(str))
        {
            return str;
        }
        final Pattern pattern;
        if (regexOrNull == null)
        {
            pattern = ONE_OR_MORE_DIGITS;
        } else
        {
            assert regexOrNull.pattern().indexOf("(\\d+)") > -1
                    || regexOrNull.pattern().indexOf("([0-9]+)") > -1;
            pattern = regexOrNull;
        }

        final Matcher matcher = pattern.matcher(str);
        boolean found = matcher.find();
        if (found == false)
        {
            final String newStr;
            if (StringUtils.isEmpty(defaultStrOrNull) == false)
            {
                newStr = defaultStrOrNull;
            } else
            {
                newStr = str + "1";
            }
            return createUniqueString(newStr, checker, pattern, defaultStrOrNull);
        }
        final StringBuilder builder = new StringBuilder();
        int nextStart = 0;
        while (found)
        {
            final String group = matcher.group(1);
            final int newNumber = Integer.parseInt(group) + 1;
            builder.append(str.substring(nextStart, matcher.start(1))).append(newNumber);
            nextStart = matcher.end(1);
            found = matcher.find();
        }
        builder.append(str.substring(nextStart));
        final String newStr = builder.toString();
        if (checker.isUnique(newStr) == false)
        {
            return createUniqueString(newStr, checker, pattern, defaultStrOrNull);
        }
        return newStr;
    }
    
    /**
     * Convert a string of hexadecimal numbers into a byte array.
     */
    public static byte[] parseHexString(String hexString)
    {
        return DatatypeConverter.parseHexBinary(hexString);
    }
    
    /**
     * Converts a byte array into a string of hexadecimal numbers.
     */
    public static String asHexString(byte[] bytesOrNull)
    {
        return bytesOrNull == null ? null : DatatypeConverter.printHexBinary(bytesOrNull);
    }
    


}
