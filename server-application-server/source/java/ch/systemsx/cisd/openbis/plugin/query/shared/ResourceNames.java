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

package ch.systemsx.cisd.openbis.plugin.query.shared;

/**
 * Resource name used in <i>query</i> plug-in.
 * <p>
 * Be aware about the uniqueness of the bean names loaded by <i>Spring</i>. Names defined here should not conflict with already existing bean names.
 * Look for other <code>ResourceNames</code> classes.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public class ResourceNames
{
    public final static String QUERY_PLUGIN_SERVICE = "query-plugin-service";

    public final static String QUERY_PLUGIN_SERVER = "query-plugin-server";

    public final static String QUERY_DATABASE_DEFINITION_PROVIDER = "query-db-definition-provider";

    private ResourceNames()
    {
    }

}
