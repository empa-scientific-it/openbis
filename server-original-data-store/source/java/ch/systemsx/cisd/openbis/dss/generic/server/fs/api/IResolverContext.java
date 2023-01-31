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

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;

/**
 * Provides the necessary resources and functionality for the file system resolvers.
 * 
 * @author Jakub Straszewski
 */
public interface IResolverContext
{
    /**
     * @return session token of a user who is accessing the view
     */
    public String getSessionToken();

    /**
     * @return hierarchical content provider to obtain access to data set contents
     */
    public IHierarchicalContentProvider getContentProvider();

    /**
     * @return the access to openbis metadata api
     */
    public IApplicationServerApi getApi();

    /**
     * @returns an object representing a file response pointing to a particular file node in data store server hierarchical content
     */
    public IFileSystemViewResponse createFileResponse(IHierarchicalContentNode node, IHierarchicalContent content);

    /**
     * Creates an empty directory response. To provide a listing for the directory one should call appropriate methods of
     * <code>ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IDirectoryResponse</code>
     * 
     * @returns an object representing a directory response.
     */
    public IDirectoryResponse createDirectoryResponse();

    /**
     * Creates an object that should be returned when the requested path doesn't exist or user has no access to.
     * 
     * @returns an object representing non-existing file
     */
    public IFileSystemViewResponse createNonExistingFileResponse(String errorMsg);
}
