/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * @author anttil
 */
public class NormalizedSampleIdentifier
{
    private static final class IdentifierParts
    {
        private String space;
        private String project;
        private String code;
        private boolean codeOnly;
        
        private void extractPartsFrom(String identifier)
        {
            if (identifier == null)
            {
                return;
            }
            if (identifier.startsWith("/"))
            {
                String[] splittedIdentifier = identifier.split("/");
                code = splittedIdentifier[splittedIdentifier.length - 1];
                if (splittedIdentifier.length == 4)
                {
                    project = splittedIdentifier[2];
                    space = splittedIdentifier[1];
                } else if (splittedIdentifier.length == 3)
                {
                    space = splittedIdentifier[1];
                }
            } else
            {
                code = identifier;
                codeOnly = true;
            }
        }
        
        private void extractPartsFrom(Sample sample)
        {
            code = sample.getCode();
            Project p = sample.getProject();
            Space s = sample.getSpace();
            if (p != null)
            {
                project = p.getCode();
                Space ps = p.getSpace();
                if (ps != null)
                {
                    s = ps;
                }
            }
            if (s != null)
            {
                space = s.getCode();
            }
        }
        
        public String getIdentifier()
        {
            if (code == null)
            {
                return null;
            }
            if (space == null)
            {
                return "/" + code;
            }
            if (project == null)
            {
                return "/" + space + "/" + code;
            }
            return "/" + space + "/" + project + "/" + code;
        }
        
    }
    
    private IdentifierParts containerIdentifierParts = new IdentifierParts();
    
    private IdentifierParts identifierParts = new IdentifierParts();

    public NormalizedSampleIdentifier(Sample sample)
    {
        Sample container = sample.getContainer();
        if (container != null)
        {
            containerIdentifierParts.extractPartsFrom(container);
        }
        identifierParts.extractPartsFrom(sample);
        if (identifierParts.code.contains(":"))
        {
            identifierParts.code = identifierParts.code.split(":")[1];
        }
    }

    public NormalizedSampleIdentifier(NewSample sample, String homeSpace)
    {

        String identifier = sample.getIdentifier().toUpperCase();
        identifierParts.extractPartsFrom(identifier);

        if (identifierParts.codeOnly)
        {
            if (identifierParts.space == null)
            {
                identifierParts.space = normalizeSpaceCode(sample.getDefaultSpaceIdentifier());
            }
            
            if (identifierParts.space == null)
            {
                identifierParts.space = normalizeSpaceCode(homeSpace);
            }
            
            if (identifierParts.space == null)
            {
                throw UserFailureException.fromTemplate("Cannot determine space for sample " + sample);
            }
        }

        if (identifierParts.code.contains(":"))
        {
            String[] split = identifierParts.code.split(":");
            containerIdentifierParts.code = split[0];
            identifierParts.code = split[1];
            containerIdentifierParts.space = identifierParts.space;
            containerIdentifierParts.project = identifierParts.project;

            if (identifier.contains("/") == false)
            {
//                throw new UserFailureException("Invalid sample identifier: " + identifier);
            }
        }

        if (sample.getCurrentContainerIdentifier() != null && containerIdentifierParts.space != null)
        {
            throw new UserFailureException("Container specified twice: " + sample.getIdentifier()
                    + " and " + sample.getCurrentContainerIdentifier());
        } else if (sample.getCurrentContainerIdentifier() != null)
        {
            String containerIdentifier = sample.getCurrentContainerIdentifier();
            containerIdentifierParts.extractPartsFrom(containerIdentifier);
            if (containerIdentifierParts.space == null)
            {
                containerIdentifierParts.space = identifierParts.space;
                containerIdentifierParts.project = identifierParts.project;
            }
        }
    }

    public String getSampleIdentifier()
    {
        return identifierParts.getIdentifier();
    }

    public String getContainerIdentifier()
    {
        return containerIdentifierParts.getIdentifier();
    }

    public String getContainerCode()
    {
        return containerIdentifierParts.code;
    }

    public String getCode()
    {
        return identifierParts.code;
    }

    private String normalizeSpaceCode(String spaceCode)
    {
        if (spaceCode != null && spaceCode.startsWith("/"))
        {
            return spaceCode.substring(1);
        } else
        {
            return spaceCode;
        }
    }
    
    @Override
    public String toString()
    {
        String sampleIdentifier = getSampleIdentifier();
        String containerIdentifier = getContainerIdentifier();
        return containerIdentifier == null ? sampleIdentifier : sampleIdentifier + " (Container: " + containerIdentifier + ")";
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        appendTo(builder, identifierParts);
        appendTo(builder, containerIdentifierParts);
        return builder.toHashCode();
    }
    
    private void appendTo(HashCodeBuilder builder, IdentifierParts parts)
    {
        builder.append(parts.space).append(parts.project).append(parts.code);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (o instanceof NormalizedSampleIdentifier == false)
        {
            return false;
        }
        NormalizedSampleIdentifier that = (NormalizedSampleIdentifier) o;
        EqualsBuilder builder = new EqualsBuilder();
        appendTo(builder, identifierParts, that.identifierParts);
        appendTo(builder, containerIdentifierParts, that.containerIdentifierParts);
        return builder.isEquals();
    }
    
    private void appendTo(EqualsBuilder builder, IdentifierParts parts1, IdentifierParts parts2)
    {
        builder.append(parts1.space, parts2.space);
        builder.append(parts1.project, parts2.project);
        builder.append(parts1.code, parts2.code);
    }
}
