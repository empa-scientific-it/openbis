/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared;

/**
 * Some common constants.
 *
 * @author Franz-Josef Elmer
 */
public class Constants
{

    public static final String DEFAULT_SHARE_ID = "1";

    /** property with thread names separated by delimiter */
    public static final String INPUT_THREAD_NAMES = "inputs";

    /** property with repotring plugins names separated by delimiter */
    public static final String REPORTING_PLUGIN_NAMES = "reporting-plugins";

    /** property with processing plugins names separated by delimiter */
    public static final String PROCESSING_PLUGIN_NAMES = "processing-plugins";

    /** property with search domain services names separated by delimiter */
    public static final String SEARCH_DOMAIN_SERVICE_NAMES = "search-domain-services";

    /** Key of service property which is a list of data source IDs. */
    public static final String DATA_SOURCES_KEY = "data-sources";

    /** Key of service property which is a list of servlet services. */
    public static final String PLUGIN_SERVICES_LIST_KEY = "plugin-services";

    /** Key of service property which is a list of custom services. */
    public static final String CUSTOM_SERVICES_LIST_KEY = "custom-services";

    public static String OVERVIEW_PLUGINS_SERVICES_LIST_KEY = "overview-plugins";

    /** Plugins for file system views (ftp etc.) */
    public static final String DSS_FS_PLUGIN_NAMES = "file-system-plugins";

}
