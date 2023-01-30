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

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Parses the given text in the constructor to extract the database instance and data space. The expected format is the following:
 * 
 * <pre>
 * [&lt;database-instance-code&gt;:]/&lt;space-code&gt;
 * </pre>
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 */
public final class SpaceIdentifierFactory extends AbstractIdentifierFactory
{
    public SpaceIdentifierFactory(final String textToParse) throws UserFailureException
    {
        super(textToParse);
    }

    public final SpaceIdentifier createIdentifier() throws UserFailureException
    {
        return parseSpaceIdentifier(getTextToParse());
    }

    private static SpaceIdentifier parseSpaceIdentifier(final String text)
    {
        final TokenLexer lexer = new TokenLexer(text);
        final SpaceIdentifier SpaceIdentifier = parseIdentifier(lexer);
        lexer.ensureNoTokensLeft();
        return SpaceIdentifier;
    }

    public static SpaceIdentifier parseIdentifier(final TokenLexer lexer)
    {
        final String firstToken = lexer.peek();
        if (firstToken.length() > 0)
        {
            throw createSlashMissingExcp(lexer.getOriginalText());
        }
        lexer.next();

        String groupCodeOrNull = lexer.next();
        if (groupCodeOrNull.length() == 0)
        {
            groupCodeOrNull = null; // if someone used "//CP" shortcut to reference home group
        } else
        {
            assertValidCode(groupCodeOrNull);
        }
        return new SpaceIdentifier(groupCodeOrNull);
    }

    public static String getSchema()
    {
        return SpaceIdentifier.Constants.IDENTIFIER_SEPARATOR + "<space-code>";
    }
}
