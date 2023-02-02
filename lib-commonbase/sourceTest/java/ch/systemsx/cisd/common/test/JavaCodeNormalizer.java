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

package ch.systemsx.cisd.common.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Normalizes multiline Java code. All lines are trimmed. All block comments and single line comments are removed. Consecutive lines are put into one
 * line separated by a space character. Empty lines and comment lines starting a new line in the normalized output.
 * 
 * @author Franz-Josef Elmer
 */
public class JavaCodeNormalizer
{
    private static interface IState
    {
        public IState next(PrintWriter writer, String trimmedLine);
    }

    private static final class Code implements IState
    {
        static final IState FIRST_LINE = new Code(true);

        static final IState LINE = new Code(false);

        private final boolean firstLine;

        Code(boolean firstLine)
        {
            this.firstLine = firstLine;
        }

        @Override
        public IState next(PrintWriter writer, String trimmedLine)
        {
            if (trimmedLine.length() == 0 || trimmedLine.startsWith("//"))
            {
                return FIRST_LINE;
            } else if (trimmedLine.startsWith("/*"))
            {
                return trimmedLine.endsWith("*/") ? FIRST_LINE : Comment.INSTANCE;
            }
            if (firstLine)
            {
                writer.println();
            } else
            {
                writer.print(' ');
            }
            writer.print(removeInlineComment(trimmedLine));
            return LINE;
        }

        private String removeInlineComment(String trimmedLine)
        {
            int indexOfInlineComment = trimmedLine.indexOf("//");
            if (indexOfInlineComment < 0)
            {
                return trimmedLine;
            }
            return trimmedLine.substring(0, indexOfInlineComment).trim();
        }
    }

    private static final class Comment implements IState
    {
        static final IState INSTANCE = new Comment();

        @Override
        public IState next(PrintWriter writer, String trimmedLine)
        {
            return trimmedLine.endsWith("*/") ? Code.FIRST_LINE : this;
        }
    }

    private JavaCodeNormalizer()
    {
    }

    /**
     * Returns the specified Java code in a normalized way.
     */
    public static String normalizeJavaCode(String javaCode)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        BufferedReader reader = new BufferedReader(new StringReader(javaCode));
        IState state = Code.FIRST_LINE;
        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                state = state.next(writer, line.trim());
            }
            return stringWriter.toString().trim();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }
}
