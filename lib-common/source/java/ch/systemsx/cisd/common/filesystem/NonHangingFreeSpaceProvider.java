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

package ch.systemsx.cisd.common.filesystem;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * A <code>DelegateFreeSpaceProvider</code> which does not block, computing the free space in its own thread.
 * 
 * @author Christian Ribeaud
 */
public final class NonHangingFreeSpaceProvider extends DelegateFreeSpaceProvider
{
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public NonHangingFreeSpaceProvider(final IFreeSpaceProvider freeSpaceProvider)
    {
        super(freeSpaceProvider);
    }

    //
    // DelegateFreeSpaceProvider
    //

    @Override
    public final long freeSpaceKb(final HostAwareFile path) throws IOException
    {
        final Future<Long> future =
                executorService.submit(new FreeSpaceCallable(getFreeSpaceProvider(), path));
        final ExecutionResult<Long> executionResult =
                ConcurrencyUtilities.getResult(future, TimingParameters.DEFAULT_TIMEOUT_MILLIS);
        final Long resultOrNull = executionResult.tryGetResult();
        if (resultOrNull != null)
        {
            return resultOrNull;
        } else
        {
            throw new IOException(String.format("Computing free space on '%s' failed: %s", path,
                    executionResult));
        }
    }

    //
    // Helper classes
    //

    private final static class FreeSpaceCallable implements Callable<Long>
    {
        private final IFreeSpaceProvider freeSpaceProvider;

        private final HostAwareFile path;

        FreeSpaceCallable(final IFreeSpaceProvider freeSpaceProvider, final HostAwareFile path)
        {
            this.freeSpaceProvider = freeSpaceProvider;
            this.path = path;
        }

        //
        // Callable
        //

        @Override
        public final Long call() throws Exception
        {
            return freeSpaceProvider.freeSpaceKb(path);
        }
    }
}
