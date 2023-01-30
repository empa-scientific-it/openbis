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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;

/**
 * IDs of static columns of {@link SampleBrowserGrid}.
 * 
 * @author Franz-Josef Elmer
 */
public class SampleGridColumnIDs
{
    public static final String CODE = "CODE";

    public static final String SUBCODE = "SUBCODE";

    public static final String SPACE = "SPACE";

    public static final String REGISTRATOR = "REGISTRATOR";

    public static final String REGISTRATION_DATE = "REGISTRATION_DATE";

    public static final String SAMPLE_TYPE = "SAMPLE_TYPE";

    public static final String METAPROJECTS = "METAPROJECTS";

    public static final String SAMPLE_IDENTIFIER = "SAMPLE_IDENTIFIER";

    public static final String IS_INSTANCE_SAMPLE = "IS_INSTANCE_SAMPLE";

    public static final String IS_DELETED = "IS_DELETED";

    public static final String EXPERIMENT = "EXPERIMENT";

    public static final String EXPERIMENT_IDENTIFIER = "EXPERIMENT_IDENTIFIER";

    public static final String PROJECT = "PROJECT";

    public static final String PROJECT_IDENTIFIER = "PROJECT_IDENTIFIER";

    public static final String PERM_ID = "PERM_ID";

    public static final String SHOW_DETAILS_LINK_COLUMN_NAME = "SHOW_DETAILS_LINK";

    public static final String PARENTS = "generatedFromParent";

    public static final String CONTAINER_SAMPLE = "containerParent";

    public static final String PROPERTIES_PREFIX = "property-";
}
