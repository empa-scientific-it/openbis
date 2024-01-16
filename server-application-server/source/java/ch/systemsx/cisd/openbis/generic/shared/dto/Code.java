/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.parser.BeanProperty;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A class that overrides {@link #equals(Object)}, {@link #hashCode()}, {@link #toString()} for a given <code>code</code>.
 * <p>
 * This class also implements {@link Comparable} interface.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class Code<T extends Code<T>> implements Serializable, Comparable<T>,
        IsSerializable
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final int CODE_LENGTH_MAX = 256;

    private String code;

    public final String getCode()
    {
        return code;
    }

    @BeanProperty(label = "code")
    public final void setCode(final String code)
    {
        this.code = code;
    }

    //
    // Object
    //

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Code<?> == false)
        {
            return false;
        }
        final Code<?> that = (Code<?>) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.code, code);
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(code);
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    @Override
    public int compareTo(final T o)
    {
        final String thatCode = o.getCode();
        if (code == null)
        {
            return thatCode == null ? 0 : -1;
        }
        if (thatCode == null)
        {
            return 1;
        }
        return code.compareTo(thatCode);
    }
}
