/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.Status;

/**
 * A wrapper on an {@link IImmutableCopier} that catches assertions and returns a failure status if any are encountered.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AssertionCatchingImmutableCopierWrapper implements IImmutableCopier
{
    private final IImmutableCopier wrapped;

    /**
     * Create a wrapper on copier
     * 
     * @param copier The copier to wrap.
     */
    public AssertionCatchingImmutableCopierWrapper(IImmutableCopier copier)
    {
        this.wrapped = copier;
    }

    @Override
    public Status copyImmutably(File source, File destinationDirectory, String nameOrNull)
    {
        Status result;
        try
        {
            result = wrapped.copyImmutably(source, destinationDirectory, nameOrNull);
        } catch (AssertionError e)
        {
            result = Status.createError(e.getMessage());
        }

        return result;
    }

    @Override
    public Status copyImmutably(File source, File destinationDirectory, String nameOrNull,
            CopyModeExisting mode)
    {
        Status result;
        try
        {
            result = wrapped.copyImmutably(source, destinationDirectory, nameOrNull, mode);
        } catch (AssertionError e)
        {
            result = Status.createError(e.getMessage());
        }

        return result;
    }

}
