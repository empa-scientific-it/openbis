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

public final class SampleIdentifierParts
{
    private String spaceCodeOrNull;

    private String projectCodeOrNull;

    private String containerCodeOrNull;

    public SampleIdentifierParts(String spaceCodeOrNull, String projectCodeOrNull, String containerCodeOrNull)
    {
        this.spaceCodeOrNull = spaceCodeOrNull;
        this.projectCodeOrNull = projectCodeOrNull;
        this.containerCodeOrNull = containerCodeOrNull;
    }

    public String getSpaceCodeOrNull()
    {
        return spaceCodeOrNull;
    }

    public String getProjectCodeOrNull()
    {
        return projectCodeOrNull;
    }

    public String getContainerCodeOrNull()
    {
        return containerCodeOrNull;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SampleIdentifierParts == false)
        {
            return false;
        }
        SampleIdentifierParts key = (SampleIdentifierParts) obj;
        return isEqual(spaceCodeOrNull, key.spaceCodeOrNull)
                && isEqual(projectCodeOrNull, key.projectCodeOrNull)
                && isEqual(containerCodeOrNull, key.containerCodeOrNull);
    }

    private boolean isEqual(String str1, String str2)
    {
        return str1 == null ? str1 == str2 : str1.equals(str2);
    }

    @Override
    public int hashCode()
    {
        return 37 * (37 * calcHashCode(spaceCodeOrNull) + calcHashCode(projectCodeOrNull))
                + calcHashCode(containerCodeOrNull);
    }

    private int calcHashCode(String str)
    {
        return str == null ? 0 : str.hashCode();
    }
}