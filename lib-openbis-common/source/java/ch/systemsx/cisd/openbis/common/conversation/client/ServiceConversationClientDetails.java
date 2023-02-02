/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.conversation.client;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Stores information about a service conversation client manager.
 * 
 * @author pkupczyk
 */
public class ServiceConversationClientDetails
{

    /**
     * URL that should be used for communication with the client manager.
     */
    private String url;

    /**
     * Timeout to be used for communication with the client manager.
     */
    private int timeout;

    public ServiceConversationClientDetails(String url, int timeout)
    {
        this.url = url;
        this.timeout = timeout;
    }

    public String getUrl()
    {
        return url;
    }

    public int getTimeout()
    {
        return timeout;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(url);
        builder.append(timeout);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        ServiceConversationClientDetails that = (ServiceConversationClientDetails) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(url, that.url);
        builder.append(timeout, that.timeout);
        return builder.isEquals();
    }

    @Override
    public String toString()
    {
        return "url: " + url + " timeout: " + timeout;
    }

}
