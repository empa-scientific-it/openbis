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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Parses the given text in the constructor to extract the database instance, the space and the project code.
 * 
 * <pre>
 * [[&lt;database-instance-code&gt;:]/&lt;group-code&gt;/]&lt;project-code&gt;
 * </pre>
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 */
public final class ProjectIdentifierFactory extends AbstractIdentifierFactory
{

    public ProjectIdentifierFactory(final String textToParse)
    {
        super(textToParse);
    }
    
    public static ProjectIdentifier tryGetProjectIdentifier(Sample sample)
    {
        Project project = sample.getProject();
        if (project == null)
        {
            return null;
        }
        return ProjectIdentifierFactory.parse(project.getIdentifier());
    }

    public final ProjectIdentifier createIdentifier() throws UserFailureException
    {
        return parse(getTextToParse(), null);
    }

    public final ProjectIdentifier createIdentifier(final String defaultSpace)
            throws UserFailureException
    {
        return parse(getTextToParse(), defaultSpace);
    }

    public static ProjectIdentifier parse(final String text)
    {
        return parse(text, null);
    }

    public static ProjectIdentifier parse(final String text,
            final String defaultSpace)
    {
        final TokenLexer lexer = new TokenLexer(text);
        final ProjectIdentifier projectIdentifier = parseIdentifier(lexer, defaultSpace);
        lexer.ensureNoTokensLeft();
        return projectIdentifier;
    }

    public static ProjectIdentifier parseIdentifier(final TokenLexer lexer,
            final String defaultSpace)
    {
        final SpaceIdentifier spaceIdentifier = parseSpace(lexer, defaultSpace);
        final String projectCode = assertValidCode(lexer.next());
        return create(spaceIdentifier, projectCode);
    }

    private static SpaceIdentifier parseSpace(final TokenLexer lexer, final String defaultSpace)
    {
        final String firstToken = lexer.peek();
        if (firstToken.length() > 0)
        {
            if (defaultSpace == null)
            {
                return SpaceIdentifier.createHome();
            } else
            {
                return new SpaceIdentifierFactory(defaultSpace).createIdentifier();
            }
        } else
        {
            return SpaceIdentifierFactory.parseIdentifier(lexer);
        }
    }

    private static ProjectIdentifier create(final SpaceIdentifier spaceIdentifier,
            final String projectCode)
    {
        return new ProjectIdentifier(spaceIdentifier.getSpaceCode(), projectCode);
    }

    public static String getSchema()
    {
        return "[" + SpaceIdentifierFactory.getSchema()
                + SpaceIdentifier.Constants.IDENTIFIER_SEPARATOR + "]"
                + "<project-code>";
    }
}
