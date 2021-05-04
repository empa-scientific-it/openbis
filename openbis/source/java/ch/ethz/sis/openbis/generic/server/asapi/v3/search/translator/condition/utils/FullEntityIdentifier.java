/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleIdentifierParts;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class which parses a entity identifier string.
 *
 * @author Viktor Kovtun
 */
public class FullEntityIdentifier
{
    private SampleIdentifierParts entityIdentifierParts;

    private String entityCode;

    public FullEntityIdentifier(String entityIdentifier, String homeSpaceCodeOrNull)
    {
        String[] parts = extractParts(entityIdentifier.startsWith("/") ? entityIdentifier : "/"+ entityIdentifier);
        String spaceCode = null;
        String projectCode = null;
        String containerCode = null;
        String plainEntityCode;
        String code;
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

        List<String> splitCode = splitCode(code, entityIdentifier);
        if (splitCode.size() == 2)
        {
            containerCode = splitCode.get(0);
            plainEntityCode = splitCode.get(1);
        } else
        {
            plainEntityCode = splitCode.get(0);
        }

        // Code format validation
        verifyCodePattern(spaceCode, "Space code");
        verifyCodePattern(projectCode, "Project code");
        verifyCodePattern(containerCode, "Container entity code");
        verifyCodePattern(plainEntityCode, (containerCode == null) ? "Entity code" : "Entity subcode");

        entityCode = CodeConverter.tryToDatabase(plainEntityCode);
        entityIdentifierParts = new SampleIdentifierParts(CodeConverter.tryToDatabase(spaceCode),
                CodeConverter.tryToDatabase(projectCode), CodeConverter.tryToDatabase(containerCode));
    }

    private List<String> splitCode(String code, String entityIdentifier)
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
            throw new IllegalArgumentException("Entity code can not contain more than one '" + delim + "': "
                    + entityIdentifier);
        }
        if (numberOfDelims != tokens.size() - 1)
        {
            throw new IllegalArgumentException("Entity code starts or ends with '" + delim + "': " + entityIdentifier);
        }
        return tokens;
    }

    private String[] extractParts(String entityIdentifier)
    {
        if (StringUtils.isBlank(entityIdentifier))
        {
            throw new IllegalArgumentException("Unspecified entity identifier.");
        }

        String[] parts = entityIdentifier.split("/");

        if (parts.length == 0)
        {
            throw new IllegalArgumentException("Entity identifier don't contain any codes: " + entityIdentifier);
        } else if (parts.length > 4)
        {
            throw new IllegalArgumentException("Entity identifier can not contain more than three '/': " + entityIdentifier);
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
    }

    public SampleIdentifierParts getParts()
    {
        return entityIdentifierParts;
    }

    public String getEntityCode()
    {
        return entityCode;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();

        final String projectCode = entityIdentifierParts.getProjectCodeOrNull();
        final String spaceCode = entityIdentifierParts.getSpaceCodeOrNull();

        final String containerCode = entityIdentifierParts.getContainerCodeOrNull();

        if (projectCode != null)
        {
            sb.append('/').append(projectCode);
        }

        if (spaceCode != null)
        {
            sb.append('/').append(spaceCode);
        }

        if (projectCode != null || spaceCode != null)
        {
            sb.append('/');
        }

        if (containerCode != null)
        {
            sb.append(containerCode).append(':');
        }

        sb.append(entityCode);

        return sb.toString();
    }

}
