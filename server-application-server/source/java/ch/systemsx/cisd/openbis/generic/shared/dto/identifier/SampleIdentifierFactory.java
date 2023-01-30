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

import static ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier.Constants;

/**
 * Parses the given text in the constructor to extract the database instance, the group and the sample code.
 * 
 * <pre>
 * /[&lt;space code&gt;/][&lt;project code&gt;/]&lt;sample code&gt;
 * </pre>
 * 
 * @author Tomasz Pylak
 */
public final class SampleIdentifierFactory extends AbstractIdentifierFactory
{
    public static final SampleIdentifier parse(final String textToParse)
            throws UserFailureException
    {
        return new SampleIdentifierFactory(textToParse).createIdentifier(null);
    }

    public static final SampleIdentifier parse(final Sample sample) throws UserFailureException
    {
        return new SampleIdentifierFactory(sample.getIdentifier()).createIdentifier(null);
    }

    public static final SampleIdentifier parse(final NewSample sample) throws UserFailureException
    {
        SampleIdentifierFactory factory = new SampleIdentifierFactory(sample.getIdentifier());
        String defaultSpace = sample.getDefaultSpaceIdentifier();
        SampleIdentifier identifier = factory.createIdentifier(defaultSpace);
        String currentContainer = sample.getCurrentContainerIdentifier();

        // if current container is defined and identifier includes container code, throw an
        // exception
        if (false == StringUtils.isEmpty(currentContainer)
                && false == StringUtils.isBlank(identifier.tryGetContainerCode()))
        {
            throw new UserFailureException("Current container is specified, but the identifier '"
                    + sample.getIdentifier() + "' includes the container code.");
        }
        if (identifier.tryGetContainerCode() == null
                && StringUtils.isEmpty(currentContainer) == false)
        {
            SampleIdentifier defaultContainerIdentifier = parse(currentContainer, defaultSpace);
            identifier.addContainerCode(defaultContainerIdentifier.getSampleSubCode());
        }
        return identifier;
    }

    public static final SampleIdentifier parse(final String textToParse, final String defaultSpace)
            throws UserFailureException
    {
        return new SampleIdentifierFactory(textToParse).createIdentifier(defaultSpace);
    }

    public static final SampleIdentifierPattern parsePattern(final String textToParse)
            throws UserFailureException
    {
        return new SampleIdentifierFactory(textToParse).createPattern();
    }

    public SampleIdentifierFactory(final String textToParse)
    {
        super(textToParse);
    }

    public final SampleIdentifier createIdentifier() throws UserFailureException
    {
        return createIdentifier(null);
    }

    public final SampleIdentifier createIdentifier(String defaultSpace) throws UserFailureException
    {
        SampleIdentifierOrPattern ident = parse(getTextToParse(), defaultSpace, false);
        return SampleIdentifier.createOwnedBy(ident.getOwner(), ident.getCode());
    }

    private SampleIdentifierPattern createPattern()
    {
        SampleIdentifierOrPattern ident = parse(getTextToParse(), null, true);
        return SampleIdentifierPattern.createOwnedBy(ident.getOwner(), ident.getCode());
    }

    private static class SampleIdentifierOrPattern
    {
        private final String code;

        private final SampleOwnerIdentifier owner;

        public SampleIdentifierOrPattern(String code, SampleOwnerIdentifier owner)
        {
            this.code = code;
            this.owner = owner;
        }

        public String getCode()
        {
            return code;
        }

        public SampleOwnerIdentifier getOwner()
        {
            return owner;
        }
    }

    private static SampleIdentifierOrPattern parse(String text, String defaultSpace,
            boolean isPattern)
    {
        String tokens[] = text.split(IDENTIFIER_SEPARARTOR_STRING);
        if (tokens.length == 0)
        {
            throw new UserFailureException(ILLEGAL_EMPTY_IDENTIFIER);
        }
        String sampleCode = tokens[tokens.length - 1];
        String[] ownerTokens = (String[]) ArrayUtils.subarray(tokens, 0, tokens.length - 1);
        SampleOwnerIdentifier owner = parseSampleOwner(ownerTokens, text, defaultSpace);
        validateSampleCode(sampleCode, isPattern);
        return new SampleIdentifierOrPattern(sampleCode, owner);
    }

    private static void validateSampleCode(String sampleCode, boolean isPattern)
    {
        String[] sampleCodeTokens = sampleCode.split(CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING);
        if (sampleCodeTokens.length > 2)
        {
            throw UserFailureException.fromTemplate(
                    AbstractIdentifierFactory.ILLEGAL_CODE_CHARACTERS_TEMPLATE, sampleCode);
        }
        for (String token : sampleCodeTokens)
        {
            validateSampleCodeToken(token, isPattern);
        }
    }

    private static void validateSampleCodeToken(String token, boolean isPattern)
    {
        if (isPattern)
        {
            assertValidPatternCharacters(token);
        } else
        {
            assertValidCode(token);
        }
    }

    private static SampleOwnerIdentifier parseSampleOwner(String[] tokens, String originalText,
            String defaultSpace)
    {
        if (tokens.length == 0)
        {
            // case: originalText is e.g. "CP1"
            return getDefaultSpaceIdentifier(defaultSpace);
        }
        String firstToken = tokens[0];
        if (firstToken.length() == 0)
        {
            // identifier starts with a slash - it refers to a home database
            if (tokens.length == 2 && tokens[1].length() == 0)
            {
                // case: shortcut to home space, originalText is e.g. "//CP1"
                return getDefaultSpaceIdentifier(defaultSpace);
            }
            return continueParsingSampleOwner(tokens, originalText);
        } else
        // identifier does not start with a slash
        {
            throw createSlashMissingExcp(originalText);
        }
    }

    private static SampleOwnerIdentifier getDefaultSpaceIdentifier(String defaultSpace)
    {
        if (defaultSpace == null)
        {
            return new SampleOwnerIdentifier(SpaceIdentifier.createHome());
        } else
        {
            return new SampleOwnerIdentifier(
                    new SpaceIdentifierFactory(defaultSpace).createIdentifier());
        }
    }

    // tries to parse owner space if there is any
    private static SampleOwnerIdentifier continueParsingSampleOwner(String[] tokens,
            String originalText)
    {
        if (tokens.length == 1)
        {
            // case: originalText is e.g. "db:/CP1" or "/CP1"
            return new SampleOwnerIdentifier();
        } else if (tokens.length == 2)
        {
            // case: originalText is e.g. "db:/space/CP1" or "/space/CP1"
            String spaceCode = tokens[1];
            return new SampleOwnerIdentifier(new SpaceIdentifier(
                    assertValidCode(spaceCode)));
        } else if (tokens.length == 3)
        {
            // case: originalText is e.g. "/space/project/CP1"
            String spaceCode = tokens[1];
            String projectCode = tokens[2];
            return new SampleOwnerIdentifier(new ProjectIdentifier(
                    assertValidCode(spaceCode), assertValidCode(projectCode)));
        } else
            throw createTooManyTokensExcp(originalText);
    }
}
