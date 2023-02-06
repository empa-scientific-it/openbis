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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Metaproject identifier representation. The identifier consists of metaproject owner id and metaproject name, e.g. "/MY_USER/MY_METAPROJECT".
 * 
 * @author pkupczyk
 */
@JsonObject("MetaprojectIdentifier")
public class MetaprojectIdentifier implements Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String SEPARATOR = "/";

    private String metaprojectOwnerId;

    private String metaprojectName;

    public MetaprojectIdentifier(String metaprojectOwnerId, String metaprojectName)
    {
        setMetaprojectOwnerId(metaprojectOwnerId);
        setMetaprojectName(metaprojectName);
    }

    public String getMetaprojectOwnerId()
    {
        return metaprojectOwnerId;
    }

    public String getMetaprojectName()
    {
        return metaprojectName;
    }

    public String format()
    {
        return SEPARATOR + metaprojectOwnerId + SEPARATOR + metaprojectName;
    }

    public static MetaprojectIdentifier parse(String str)
    {
        if (str == null)
        {
            return null;
        }

        String[] splitted = str.split(SEPARATOR);

        if (splitted.length == 3)
        {
            return new MetaprojectIdentifier(splitted[1], splitted[2]);
        } else
        {
            throw new IllegalArgumentException("Metaproject identifier must have " + SEPARATOR
                    + "USER_ID" + SEPARATOR + "METAPROJECT_NAME format - was " + str);
        }
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private MetaprojectIdentifier()
    {
    }

    private void setMetaprojectOwnerId(String metaprojectOwnerId)
    {
        if (metaprojectOwnerId == null)
        {
            throw new IllegalArgumentException("Metaproject owner id cannot be null");
        }
        this.metaprojectOwnerId = metaprojectOwnerId;
    }

    private void setMetaprojectName(String metaprojectName)
    {
        if (metaprojectName == null)
        {
            throw new IllegalArgumentException("Metaproject name cannot be null");
        }
        this.metaprojectName = metaprojectName;
    }

}
