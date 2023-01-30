/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * A <code>SampleParentWithDerived</code> encapsulates a <code>Sample</code> (the parent) and its derived <code>Sample</code>s.
 * 
 * @author Christian Ribeaud
 */
public final class SampleParentWithDerived implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /**
     * The parent <code>Sample</code>.
     */
    private Sample parent;

    /** The derived (child) <code>Sample</code>s. */
    private Sample[] derived = Sample.EMPTY_ARRAY;

    public SampleParentWithDerived()
    {
    }

    public SampleParentWithDerived(Sample parent, Sample[] derived)
    {
        this.parent = parent;
        this.derived = derived;
    }

    /**
     * Never returns <code>null</code> but could return an empty array.
     */
    public final Sample[] getDerived()
    {
        if (derived == null)
        {
            return Sample.EMPTY_ARRAY;
        }
        return derived;
    }

    public final void setDerived(final Sample[] generated)
    {
        this.derived = generated;
    }

    public final Sample getParent()
    {
        return parent;
    }

    public final void setParent(final Sample generator)
    {
        this.parent = generator;
    }
}
