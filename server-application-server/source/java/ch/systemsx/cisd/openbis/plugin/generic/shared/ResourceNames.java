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

package ch.systemsx.cisd.openbis.plugin.generic.shared;

/**
 * Resource name used in <i>generic</i> plug-in.
 * <p>
 * Be aware about the uniqueness of the bean names loaded by <i>Spring</i>. Names defined here should not conflict with already existing bean names.
 * Look for other <code>ResourceNames</code> classes.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ResourceNames
{
    public final static String GENERIC_PLUGIN_SERVICE = "generic-plugin-service";

    public final static String GENERIC_PLUGIN_SERVER = "generic-plugin-server";

    public final static String GENERIC_BUSINESS_OBJECT_FACTORY = "generic-business-object-factory";

    private ResourceNames()
    {
        // Can not be instantiated.
    }

}
