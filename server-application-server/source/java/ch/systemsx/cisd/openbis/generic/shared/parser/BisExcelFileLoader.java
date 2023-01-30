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

package ch.systemsx.cisd.openbis.generic.shared.parser;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.NamedReader;
import ch.systemsx.cisd.common.parser.ExcelFileLoader;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.ParsingException;
import ch.systemsx.cisd.common.parser.TabFileLoader;

/**
 * A <i>openBIS</i> {@link TabFileLoader} extension which translates a {@link ParsingException} into a more user friendly {@link UserFailureException}
 * .
 * <p>
 * Note that this extension prefers to work with {@link NamedReader}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class BisExcelFileLoader<T> extends ExcelFileLoader<T>
{

    private static final String MESSAGE_FORMAT =
            "A problem has occurred while parsing line %d of file '%s':\n  %s";

    private static final String ERROR_IN_FILE_MESSAGE_FORMAT =
            "A problem has occurred while parsing file '%s':\n  %s";

    private final boolean acceptEmptyFiles;

    public BisExcelFileLoader(final IParserObjectFactoryFactory<T> factory, boolean acceptEmptyFiles)
    {
        super(factory);
        this.acceptEmptyFiles = acceptEmptyFiles;
    }

    private final static void translateParsingException(final ParsingException parsingException,
            final String sectionName)
    {
        final RuntimeException causeException = parsingException.getCauseRuntimeException();
        final String message =
                causeException == null ? parsingException.getMessage() : causeException
                        .getMessage();
        throw UserFailureException.fromTemplate(MESSAGE_FORMAT, parsingException.getLineNumber(),
                sectionName, message);
    }

    private final static void translateParserException(final ParserException ex,
            final String sectionName)
    {
        final String message = ex.getMessage();
        throw UserFailureException.fromTemplate(ERROR_IN_FILE_MESSAGE_FORMAT, sectionName, message);
    }

    //
    // TabFileLoader
    //
    public List<T> load(final Sheet sheet, int begin, int end, String sectionName,
            final Map<String, String> defaults) throws ParserException, ParsingException,
            IllegalArgumentException
    {
        assert sheet != null : "Excel Sheet cannot be null.";
        try
        {
            final List<T> list = super.load(sheet, begin, end, defaults);
            if (acceptEmptyFiles == false && list.size() == 0)
            {
                throw new UserFailureException("Given file '" + sectionName
                        + "' is empty or does not contain any meaningful information.");
            }
            return list;
        } catch (final IllegalArgumentException illegalArgumentException)
        {
            throw new UserFailureException(illegalArgumentException.getMessage(),
                    illegalArgumentException);
        } catch (final ParsingException e)
        {
            translateParsingException(e, sectionName);
            // Never used.
            return null;
        } catch (final ParserException e)
        {
            translateParserException(e, sectionName);
            // Never used.
            return null;
        }
    }
}
