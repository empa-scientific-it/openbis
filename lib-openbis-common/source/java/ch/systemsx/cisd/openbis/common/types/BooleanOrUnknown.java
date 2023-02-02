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

package ch.systemsx.cisd.openbis.common.types;

/**
 * A type which represents a boolean which could have a third state: <code>U</code> ("UNKNOWN").
 * <p>
 * The character returned by {@link #name()} is saved in the database.
 * </p>
 * 
 * @author Bernd Rinn
 */
public enum BooleanOrUnknown
{
    F("FALSE"), T("TRUE"), U("UNKNOWN");

    private final String niceRepresentation;

    BooleanOrUnknown(final String niceRepresentation)
    {
        this.niceRepresentation = niceRepresentation;
    }

    /**
     * Returns a nice representation of this value.
     */
    public final String getNiceRepresentation()
    {
        return niceRepresentation;
    }

    public final static BooleanOrUnknown resolve(final String niceRepresentationOrNull)
    {
        if (niceRepresentationOrNull == null)
        {
            return U;
        }
        if (niceRepresentationOrNull.equals("FALSE"))
        {
            return F;
        }
        if (niceRepresentationOrNull.equals("TRUE"))
        {
            return T;
        }
        if (niceRepresentationOrNull.equals("UNKNOWN"))
        {
            return U;
        }
        throw new IllegalArgumentException(String.format("Given nice representation '%s' unknown.",
                niceRepresentationOrNull));
    }

    /**
     * Resolve the specified boolean flag to either {@link #T} or {@link #F}.
     */
    public final static BooleanOrUnknown resolve(final boolean flag)
    {
        return flag ? T : F;
    }

    /**
     * Resolve the specified instance to an instance of {@link Boolean}. Returns <code>null</code> if the argument is {@link #U}.
     */
    public final static Boolean tryToResolve(final BooleanOrUnknown booleanOrUnknown)
    {
        return booleanOrUnknown == U ? null : Boolean.valueOf(booleanOrUnknown == T);
    }

}
