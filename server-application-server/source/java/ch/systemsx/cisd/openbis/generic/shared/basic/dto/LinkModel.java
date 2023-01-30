/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A model for reporting plug-ins of type DSS_LINK. It keeps the different parts of the URL separate so that users can easily use only the parts they
 * need.
 * <p>
 * The url is broken into schemAndDomain, path, and query parameters. An example: <br>
 * https://openbis.ethz.ch/datastore_server/2010093083732894?param1=482745&param2=something
 * <ul>
 * <li>https://openbis.ethz.ch <b>[schemeAndDomain]</b></li>
 * <li>datastore_server/2010093083732894 <b>[path]</b></li>
 * <li>param1=482745&param2=something <b>[parameters]</b></li>
 * </ul>
 * <p>
 * LinkModels returned by the server do not (necessarily) contain a sessionID. The client should provide the session Id to the LinkModel before using
 * it.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class LinkModel implements Serializable
{
    // Package visible constant
    static final String SESSION_ID_PARAMETER_NAME = "sessionID";

    private static final long serialVersionUID = 1L;

    private String schemeAndDomain;

    private String path;

    private ArrayList<LinkParameter> parameters = new ArrayList<LinkParameter>();

    public static class LinkParameter implements IsSerializable, Serializable
    {

        private static final long serialVersionUID = 1L;

        private String name;

        private String value;

        public LinkParameter()
        {

        }

        public LinkParameter(String name, String value)
        {
            this.setName(name);
            this.setValue(value);
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

    }

    /**
     * Default Constructor.
     */
    public LinkModel()
    {

    }

    /**
     * Set the scheme and domain of the url
     * 
     * @param schemeAndDomain e.g., https://openbis.ethz.ch
     */
    public void setSchemeAndDomain(String schemeAndDomain)
    {
        this.schemeAndDomain = schemeAndDomain;
    }

    public String getSchemeAndDomain()
    {
        return schemeAndDomain;
    }

    /**
     * Set the path of the url
     * 
     * @param path e.g., datastore_server/2010093083732894
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    /**
     * Set the parameters
     */
    public void setParameters(List<LinkParameter> parameters)
    {
        this.parameters.clear();
        this.parameters.addAll(parameters);
    }

    public List<LinkParameter> getParameters()
    {
        return parameters;
    }

    /**
     * The session token is a special parameter and should be set this way.
     */
    public void setSessionId(String sessionId)
    {
        // See if the parameter has already been added
        LinkParameter sessionToken = trySessionTokenParameter();
        if (null != sessionToken)
        {
            // Set the new value
            sessionToken.setValue(sessionId);
            // We're done
            return;
        }

        // If we are here, we need to create the sessionToken
        sessionToken = new LinkParameter(SESSION_ID_PARAMETER_NAME, sessionId);
        parameters.add(sessionToken);
    }

    /**
     * The session token is a special parameter. It is returned as one of the parameters by {@link #getParameters()}, but it can be retrieved directly
     * this way.
     */
    public String trySessionId()
    {
        LinkParameter sessionTokenOrNull = trySessionTokenParameter();

        return (null == sessionTokenOrNull) ? null : sessionTokenOrNull.getValue();
    }

    private LinkParameter trySessionTokenParameter()
    {
        for (LinkParameter param : parameters)
        {
            if (SESSION_ID_PARAMETER_NAME.equals(param.getName()))
            {
                return param;
            }
        }
        return null;
    }

}
