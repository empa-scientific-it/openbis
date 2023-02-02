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

package ch.systemsx.cisd.openbis.plugin.screening.shared;

/**
 * Resource name used in <i>screening</i> plug-in.
 * <p>
 * Be aware about the uniqueness of the bean names loaded by <i>Spring</i>. Names defined here should not conflict with already existing bean names.
 * Look for other <code>ResourceNames</code> classes.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class ResourceNames
{
    private ResourceNames()
    {
        // Can not be instantiated.
    }

    public final static String SCREENING_PLUGIN_SERVICE = "screening-plugin-service";

    public final static String SCREENING_PLUGIN_SERVER = "screening-plugin-server";

    public final static String SCREENING_BUSINESS_OBJECT_FACTORY = "screening-bo-factory";

    public final static String SCREENING_DAO_FACTORY = "screening-dao-factory";

    public final static String SCREENING_SAMPLE_SERVER_PLUGIN = "screening-sample-server-plugin";
}
