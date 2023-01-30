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

package ch.systemsx.cisd.openbis.jstest.server;

/**
 * @author pkupczyk
 */
public class JsTestDataStoreServer2 extends JsTestDataStoreServer
{

    public JsTestDataStoreServer2()
    {
        setName("DSS2");
        setRootPath("../datastore_server2");
        setDumpsPath("../datastore_server2/db");
        setPort(20002);
        setDebugPort(20012);
    }

}
