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
package ch.ethz.sis.filetransfer;

/**
 * A retry provider interface. A retry provider is responsible for retrying actions if they fail. Depending on a use case actual implementations may
 * decide to retry for different kinds of failures, allow different number of attempts before a final failure or use different waiting times between
 * each retry.
 * 
 * @author pkupczyk
 */
public interface IRetryProvider
{

    /**
     * Executes a given action with potential retry logic in case of failure.
     * 
     * @throws DownloadException In case of any problems
     */
    public <T> T executeWithRetry(IRetryAction<T> action) throws DownloadException;

}
