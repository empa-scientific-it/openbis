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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * A <code>SampleParentWithDerivedDTO</code> encapsulates a <code>SamplePE</code> (the parent) and its derived <code>SamplePE</code>s.
 * 
 * @author Christian Ribeaud
 */
public final class SampleParentWithDerivedDTO implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    /**
     * The <code>SamplePE</code> parent.
     */
    private SamplePE parent;

    /**
     * The <code>Sample</code> children.
     * <p>
     * Never <code>null</code> and default value is an empty array.
     * </p>
     */
    private SamplePE[] derived = SamplePE.EMPTY_ARRAY;

    public SampleParentWithDerivedDTO()
    {

    }

    public SampleParentWithDerivedDTO(final SamplePE generator, final List<SamplePE> generated)
    {
        assert generator != null : "Generator can not be null.";
        assert generated != null : "Generated can not be null.";
        setParent(generator);
        setDerived(generated.toArray(SamplePE.EMPTY_ARRAY));
    }

    /**
     * Never returns <code>null</code> but could return an empty list.
     */
    public final SamplePE[] getDerived()
    {
        return derived;
    }

    public final SamplePE getParent()
    {
        return parent;
    }

    public void setParent(final SamplePE generator)
    {
        this.parent = generator;
    }

    public void setDerived(final SamplePE[] generated)
    {
        this.derived = generated;
    }
}
