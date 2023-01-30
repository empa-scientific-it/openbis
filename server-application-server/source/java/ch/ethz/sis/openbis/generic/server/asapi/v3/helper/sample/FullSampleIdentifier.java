/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

/**
 * Helper class which parses a sample identifier string. It offers human readable errors for incorrect inputs.
 * 
 * @author Franz-Josef Elmer
 */
public class FullSampleIdentifier
{
    private static final String CODE_CHAR_PATTERN = "a-zA-Z0-9_\\-\\.";

    private static final String CODE_CHARS = "[" + CODE_CHAR_PATTERN + "]+";

    private static final String CODE_PATTERN = "^" + CODE_CHARS + "$";

    private SampleIdentifierParts sampleIdentifierParts;

    private String sampleCode;

    public FullSampleIdentifier(String sampleIdentifier, String homeSpaceCodeOrNull)
    {
        String[] parts = extractParts(sampleIdentifier);
        String spaceCode = null;
        String projectCode = null;
        String containerCode = null;
        String plainSampleCode = null;
        String code = null;
        if (parts.length == 2)
        {
            code = parts[1];
        } else if (parts.length == 3)
        {
            spaceCode = parts[1];
            code = parts[2];
        } else
        {
            spaceCode = parts[1];
            projectCode = parts[2];
            code = parts[3];
        }
        if (spaceCode != null && spaceCode.isEmpty() && homeSpaceCodeOrNull != null)
        {
            spaceCode = homeSpaceCodeOrNull;
        }

        List<String> splittedCode = splitCode(code, sampleIdentifier);
        if (splittedCode.size() == 2)
        {
            containerCode = splittedCode.get(0);
            plainSampleCode = splittedCode.get(1);
        } else
        {
            plainSampleCode = splittedCode.get(0);
        }

        // Code format validation
        verifyCodePattern(spaceCode, "Space code");
        verifyCodePattern(projectCode, "Project code");
        verifyCodePattern(containerCode, "Container sample code");
        verifyCodePattern(plainSampleCode, containerCode == null ? "Sample code" : "Sample subcode");

        sampleCode = CodeConverter.tryToDatabase(plainSampleCode);
        sampleIdentifierParts = new SampleIdentifierParts(CodeConverter.tryToDatabase(spaceCode),
                CodeConverter.tryToDatabase(projectCode), CodeConverter.tryToDatabase(containerCode));

    }

    private List<String> splitCode(String code, String sampleIdentifier)
    {
        String delim = ":";
        StringTokenizer tokenizer = new StringTokenizer(code, delim, true);
        int numberOfDelims = 0;
        List<String> tokens = new ArrayList<>();
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            if (delim.equals(token))
            {
                numberOfDelims++;
            } else
            {
                tokens.add(token);
            }
        }
        if (numberOfDelims > 1)
        {
            throw new IllegalArgumentException("Sample code can not contain more than one '" + delim + "': "
                    + sampleIdentifier);
        }
        if (numberOfDelims != tokens.size() - 1)
        {
            throw new IllegalArgumentException("Sample code starts or ends with '" + delim + "': " + sampleIdentifier);
        }
        return tokens;
    }

    private String[] extractParts(String sampleIdentifier)
    {
        if (StringUtils.isBlank(sampleIdentifier))
        {
            throw new IllegalArgumentException("Unspecified sample identifier.");
        }
        if (sampleIdentifier.startsWith("/") == false)
        {
            throw new IllegalArgumentException("Sample identifier has to start with a '/': " + sampleIdentifier);
        }

        String[] parts = sampleIdentifier.split("/");

        if (parts.length == 0)
        {
            throw new IllegalArgumentException("Sample identifier don't contain any codes: " + sampleIdentifier);
        } else if (parts.length > 4)
        {
            throw new IllegalArgumentException("Sample identifier can not contain more than three '/': " + sampleIdentifier);
        }
        return parts;
    }

    private void verifyCodePattern(String code, String partName)
    {
        if (code == null)
        {
            return;
        }
        if (code.length() == 0)
        {
            throw new IllegalArgumentException(partName + " can not be an empty string.");
        }
        if (code.matches(CODE_PATTERN) == false)
        {
            throw new IllegalArgumentException(partName + " containing other characters than letters, numbers, "
                    + "'_', '-' and '.': " + code);
        }
    }

    public SampleIdentifierParts getParts()
    {
        return sampleIdentifierParts;
    }

    public String getSampleCode()
    {
        return sampleCode;
    }

    @Override
    public String toString()
    {
        return new SampleIdentifier(sampleIdentifierParts.getSpaceCodeOrNull(),
                sampleIdentifierParts.getProjectCodeOrNull(), sampleIdentifierParts.getContainerCodeOrNull(),
                sampleCode).toString();
    }

}
