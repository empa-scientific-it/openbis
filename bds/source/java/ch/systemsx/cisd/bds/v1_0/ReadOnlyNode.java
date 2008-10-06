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

package ch.systemsx.cisd.bds.v1_0;

import java.io.File;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.bds.storage.NodeProxy;

/**
 * An {@link IDirectory} implementation which denies access to write operations.
 * 
 * @author Christian Ribeaud
 */
public final class ReadOnlyNode extends NodeProxy
{
    public ReadOnlyNode(final INode delegate)
    {
        super(delegate);
    }

    final static ReadOnlyNode tryCreateReadOnlyNode(final INode node)
    {
        if (node == null)
        {
            return null;
        }
        return new ReadOnlyNode(node);
    }

    final static void denyAccess()
    {
        throw new UnsupportedOperationException("Unsupported operation in read-only mode.");
    }

    //
    // NodeProxy
    //

    @Override
    public final void moveTo(final File directory)
    {
        denyAccess();
    }

    @Override
    public final IDirectory tryAsDirectory()
    {
        return ReadOnlyDirectory.tryCreateReadOnlyDirectory(super.tryAsDirectory());
    }

    @Override
    public final IFile tryAsFile()
    {
        return ReadOnlyFile.tryCreateReadOnlyFile(super.tryAsFile());
    }

    @Override
    public final IDirectory tryGetParent()
    {
        return ReadOnlyDirectory.tryCreateReadOnlyDirectory(super.tryGetParent());
    }
}
