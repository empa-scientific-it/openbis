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
package ch.systemsx.cisd.common.filesystem;

import java.io.File;

/**
 * A <code>IStoreHandler</code> implementation which adapts encapsulated {@link IPathHandler}.
 * 
 * @author Christian Ribeaud
 */
public class PathHandlerAdapter implements IStoreHandler
{
    private final IPathHandler pathHandler;

    private final File directory;

    public PathHandlerAdapter(final IPathHandler pathHandler, final File directory)
    {
        this.pathHandler = pathHandler;
        this.directory = directory;
    }

    final static IStoreHandler asScanningHandler(final File directory, final IPathHandler handler)
    {
        return new PathHandlerAdapter(handler, directory);
    }

    private final File asFile(final StoreItem item)
    {
        return StoreItem.asFile(directory, item);
    }

    //
    // IStoreHandler
    //

    @Override
    public final boolean handle(final StoreItem item)
    {
        final File file = asFile(item);
        pathHandler.handle(file);
        return file.exists() == false;
    }

    @Override
    public boolean isStopped()
    {
        return pathHandler.isStopped();
    }

}
