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

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.systemsx.cisd.common.string.StringUtilities;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;

/**
 * Identifies a data space.
 * 
 * @author Izabela Adamczyk
 */
public class SpaceIdentifier implements
        Comparable<SpaceIdentifier>, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    public final static class Constants
    {
        public static final char IDENTIFIER_SEPARATOR = '/';
    }

    private String spaceCodeOrNull;

    public static SpaceIdentifier createHome()
    {
        return new SpaceIdentifier(getHomeSpaceCode());
    }

    /** space in the home database */
    public SpaceIdentifier(final String spaceCode)
    {
        setSpaceCode(spaceCode);
    }

    public final String getSpaceCode()
    {
        return StringUtils.upperCase(spaceCodeOrNull);
    }

    public final void setSpaceCode(final String spaceCode)
    {
        this.spaceCodeOrNull = spaceCode;
    }

    //
    // DatabaseInstanceIdentifier
    //

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SpaceIdentifier == false)
        {
            return false;
        }
        final SpaceIdentifier that = (SpaceIdentifier) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getSpaceCode(), that.getSpaceCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSpaceCode());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return Constants.IDENTIFIER_SEPARATOR + spaceCodeOrNull;
    }

    protected static String getHomeSpaceCode()
    {
        return SpaceCodeHelper.HOME_SPACE_CODE;
    }

    public boolean isHomeSpace()
    {
        return SpaceCodeHelper.isHomeSpace(spaceCodeOrNull);
    }

    // GWT only
    @SuppressWarnings("unused")
    private SpaceIdentifier()
    {
        super();
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final SpaceIdentifier other)
    {
        return StringUtilities.compareNullable(getSpaceCode(), other.getSpaceCode());
    }
}
