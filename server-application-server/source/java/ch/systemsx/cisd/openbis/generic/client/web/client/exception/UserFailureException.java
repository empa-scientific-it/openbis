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

package ch.systemsx.cisd.openbis.generic.client.web.client.exception;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The <code>UserFailureException</code> is the super class of all exceptions that have their cause in an inappropriate usage of the system. This
 * implies that the user himself (without help of an administrator) can fix the problem.
 * 
 * @author Christian Ribeaud
 */
public class UserFailureException extends RuntimeException implements IsSerializable
{
    private static final long serialVersionUID = 1L;

    private String details;

    // An non-empty constructor is mandatory in GWT for serializable objects
    public UserFailureException()
    {
    }

    public UserFailureException(final String message, final String detailsOrNull)
    {
        super(message);
        this.details = detailsOrNull;
    }

    public UserFailureException(final String message)
    {
        this(message, null);
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails(String details)
    {
        this.details = details;
    }

}
