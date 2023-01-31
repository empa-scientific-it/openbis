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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization;

import java.io.File;

import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;

/**
 * The IDssServiceRpcGeneric interface extended with methods that are internal to the DSS server.
 * <p>
 * This interface is necessary because the implementation of IDssServiceRpcGeneric that is visible to the {@link DataStoreServer} is a proxy. The
 * methods here are used internally, but need to be known to the proxy as well. (See the dssApplicationContext.xml file as well.)
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDssServiceRpcGenericInternal extends IDssServiceRpcGeneric
{
    public void setStoreDirectory(File aFile);

}
