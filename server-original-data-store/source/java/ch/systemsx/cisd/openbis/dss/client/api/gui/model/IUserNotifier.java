/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui.model;

/**
 * Notifies the user of the given <var>throwable</var>, if the error message is different from <var>lastExceptionMessageOrNull</var>.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Franz-Josef Elmer
 */
public interface IUserNotifier
{
    public String notifyUserOfThrowable(final String fileName, final String operationName,
            final Throwable throwable, final String lastExceptionMessageOrNull);
}
