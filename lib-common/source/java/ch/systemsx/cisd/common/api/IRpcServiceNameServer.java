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

package ch.systemsx.cisd.common.api;

import java.util.Collection;

/**
 * An Interface for finding out about the IRpcService interfaces supported by a server.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IRpcServiceNameServer extends IRpcService
{
    /**
     * The preferred suffix of the url of the name server service
     */
    public static String PREFFERED_URL_SUFFIX = "/rmi-name-server";

    /**
     * The preferred name of the bean in Spring
     */
    public static String PREFFERED_BEAN_NAME = "rpc-name-server";

    /**
     * The preferred name of the bean in Spring
     */
    public static String PREFFERED_SERVICE_NAME = "name-server";

    /**
     * Return a collection of interfaces supported by this server
     */
    Collection<RpcServiceInterfaceDTO> getSupportedInterfaces();
}
