/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.api;

import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;

/**
 * File system response resolver. The implementation should be able to resolve paths into files or directories.
 * 
 * @author Jakub Straszewski
 */
public interface IResolver
{
    /**
     * Create a file system response resolving the local path specified as an array of path items.
     * 
     * @param pathItems relative path that this resolver is expected to resolve represented as an arary (path <code>"ONE/TWO/THREE"</code> is
     *            represented as <code>{"ONE", "TWO", "THREE"}</code>
     * @param resolverContext contains required resources and functionality to provide response
     */
    IFileSystemViewResponse resolve(String[] pathItems, IResolverContext resolverContext);
}