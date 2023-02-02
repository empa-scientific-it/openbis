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

package ch.systemsx.cisd.common.action;

/**
 * A <code>ITerminable</code> implementation which could accept several {@link ITerminable}.
 * 
 * @author Christian Ribeaud
 */
public class CompoundTerminable implements ITerminable
{
    private final ITerminable[] terminables;

    public CompoundTerminable(final ITerminable... terminables)
    {
        assert terminables != null : "Unspecified set of ITerminable";
        this.terminables = terminables;
    }

    /**
     * Terminates given {@link ITerminable}.
     */
    protected boolean terminate(final ITerminable terminable)
    {
        assert terminable != null : "Unspecified ITerminable";
        return terminable.terminate();
    }

    //
    // ITerminable
    //

    @Override
    public boolean terminate()
    {
        boolean ok = true;
        for (final ITerminable terminable : terminables)
        {
            ok = ok && terminate(terminable);
        }
        return ok;
    }

}
