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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

/**
 * Interface of objects providing messages.
 * 
 * @author Franz-Josef Elmer
 */
public interface IMessageProvider
{
    /**
     * Returns the name of the message provider.
     */
    public String getName();

    /**
     * Returns the message specified by a key. Optional parameters are used to render the message by replacing <code>{0}</code> by the first
     * parameter, <code>{1}</code> by the second one etc.
     * <p>
     * If a message couldn't be found an error message is returned which includes the invalid key and information about the message source of this
     * provider.
     */
    public String getMessage(String key, Object... parameters);

    /** Whether this implementation contains given <var>key</var>. */
    public boolean containsKey(String key);

}
