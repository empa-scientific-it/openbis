/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.utilities;

/**
 * Extension of {@link ITimeProvider} which abstracts sleeping for a specified time.
 *
 * @author Franz-Josef Elmer
 */
public interface ITimeAndWaitingProvider extends ITimeProvider
{
    /**
     * Sleeps the specified number of milliseconds.
     */
    public void sleep(long milliseconds);
}