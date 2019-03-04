/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.filetransfer;

import java.util.List;

/**
 * A concurrency provider interface. A concurrency provider is responsible for controlling concurrency of downloads (e.g. decide how many parallel
 * download streams are allowed). Depending on a use case actual implementations may take different criteria into consideration when deciding on
 * allowed concurrency (e.g. total number of existing downloads, number of existing downloads for a given user, server memory, server bandwidth etc.)
 * 
 * @author pkupczyk
 */
public interface IConcurrencyProvider
{

    /**
     * Decides how many concurrent download streams will be allowed for a download which is about to start. The decision can be made basing on a user
     * that is about to perform a download, wished number of download streams that the user has requested for, states of existing downloads or any
     * other criteria like server memory or server bandwidth.
     * 
     * @return Number of allowed download streams. If the returned number is <= 0 then the whole download will be rejected with appropriate error
     *         message.
     * @throws DownloadException In case of any problems
     */
    public int getAllowedNumberOfStreams(IUserSessionId userSessionId, Integer wishedNumberOfStreamsOrNull, List<DownloadState> downloadStates)
            throws DownloadException;

}
