/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang;

/**
 * <p>
 * Escapes and unescapes <code>String</code>s for HTML.
 * </p>
 * <p>
 * This is a small part of implementation taken from org.apache.commons.lang with java.io dependencies removed.
 * </p>
 * <p>
 * See {@link Entities} for update to a new version of apache.commons.lang instructions. This class should rather be left without changes.
 * </p>
 * 
 * @see Entities
 * @author Piotr Buczek
 */
public class StringEscapeUtils
{

    /**
     * <p>
     * Escapes the characters in a <code>String</code> using HTML entities.
     * </p>
     * <p>
     * For example:
     * </p>
     * <p>
     * <code>"bread" & "butter"</code>
     * </p>
     * becomes:
     * <p>
     * <code>&amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;</code>.
     * </p>
     * <p>
     * Supports all known HTML 4.0 entities, including funky accents. Note that the commonly used apostrophe escape character (&amp;apos;) is not a
     * legal entity and so is not supported).
     * </p>
     * 
     * @param str the <code>String</code> to escape, may be null
     * @return a new escaped <code>String</code>, <code>null</code> if null string input
     * @see #unescapeHtml(String)
     * @see <a href="http://hotwired.lycos.com/webmonkey/reference/special_characters/">ISO Entities</a>
     * @see <a href="http://www.w3.org/TR/REC-html32#latin1">HTML 3.2 Character Entities for ISO Latin-1</a>
     * @see <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">HTML 4.0 Character entity references</a>
     * @see <a href="http://www.w3.org/TR/html401/charset.html#h-5.3">HTML 4.01 Character References</a>
     * @see <a href="http://www.w3.org/TR/html401/charset.html#code-position">HTML 4.01 Code positions</a>
     */
    public static String escapeHtml(String str)
    {
        if (str == null)
        {
            return null;
        }
        return Entities.HTML40.escape(str);
    }

    /**
     * <p>
     * Unescapes a string containing entity escapes to a string containing the actual Unicode characters corresponding to the escapes. Supports HTML
     * 4.0 entities.
     * </p>
     * <p>
     * For example, the string "&amp;lt;Fran&amp;ccedil;ais&amp;gt;" will become "&lt;Fran&ccedil;ais&gt;"
     * </p>
     * <p>
     * If an entity is unrecognized, it is left alone, and inserted verbatim into the result string. e.g. "&amp;gt;&amp;zzzz;x" will become
     * "&gt;&amp;zzzz;x".
     * </p>
     * 
     * @param str the <code>String</code> to unescape, may be null
     * @return a new unescaped <code>String</code>, <code>null</code> if null string input
     */
    public final static String unescapeHtml(String str)
    {
        if (str == null)
        {
            return null;
        }
        return Entities.HTML40.unescape(str);
    }

}
