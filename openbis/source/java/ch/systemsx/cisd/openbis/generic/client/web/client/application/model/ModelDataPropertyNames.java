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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

/**
 * Some constants used in {@link ModelData} implementations. These constants are typically used in
 * {@link ColumnConfig#setId(String)}. Because they serve a different purpose, they should not be
 * used in {@link ColumnConfig#setHeader(String)}.
 * <p>
 * Use <i>Java</i> coding standard for naming these property names and be aware that some of them
 * could be use for sorting when using <i>Result Set</i>.
 * </p>
 * <p>
 * <b>Important note</b>: Do not put a <code>_</code> in the property name except for specifying
 * a field path.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ModelDataPropertyNames
{
    /** Because <i>Javascript</i> can not handle 'dot' in the property name. */
    private static final String FIELD_PATH_SEPARATOR = "_";

    public static final String CODE = "code";

    public static final String FILE_NAME = "fileName";

    public static final String VERSION = "version";

    public static final String DESCRIPTION = "description";

    public static final String EMAIL = "email";

    public static final String ENTITY_KIND = "entityKind";

    public static final String ENTITY_TYPE = "entityType";

    public static final String MATCHING_FIELD = "fieldDescription";

    public static final String MATCHING_TEXT = "textFragment";

    /**
     * This is the path to the experiment field starting from {@link Sample}.
     * <p>
     * Changing this may mean that sorting no longer works on the server side if result set is used.
     * </p>
     */
    public static final String EXPERIMENT_FOR_SAMPLE =
            "validProcedure" + FIELD_PATH_SEPARATOR + "experiment";

    /**
     * This is the path to the experiment identifier field starting from {@link Sample}.
     * <p>
     * Changing this may mean that sorting no longer works on the server side if result set is used.
     * </p>
     */
    public static final String EXPERIMENT_IDENTIFIER_FOR_SAMPLE =
            EXPERIMENT_FOR_SAMPLE + FIELD_PATH_SEPARATOR + "experimentIdentifier";

    /**
     * This is the path to the experiment type code field starting from {@link Experiment}.
     * <p>
     * Changing this may mean that sorting no longer works on the server side if result set is used.
     * </p>
     */
    public static final String EXPERIMENT_TYPE_CODE_FOR_EXPERIMENT =
            "experimentType" + FIELD_PATH_SEPARATOR + "code";

    public static final String FILE_FORMAT_TYPE = "fileFormatType";

    public static final String FIRST_NAME = "firstName";

    public static final String GROUP = "group";

    /**
     * This is the path to the project field starting from {@link Experiment}.
     * <p>
     * Changing this may mean that sorting no longer works on the server side if result set is used.
     * </p>
     */
    public static final String GROUP_FOR_EXPERIMENT = "project" + FIELD_PATH_SEPARATOR + "group";

    public static final String IDENTIFIER = "identifier";

    public static final String DATABASE_INSTANCE = "databaseInstance";

    public static final String IS_GROUP_SAMPLE = "isGroupSample";

    public static final String IS_INSTANCE_SAMPLE = "isInstanceSample";

    public static final String IS_INVALID = "isInvalid";

    public static final String LAST_NAME = "lastName";

    public static final String LEADER = "leader";

    public static final String LOCATION = "location";

    public static final String OBJECT = "object";

    public static final String PERSON = "person";

    /**
     * This is the path to the project field starting from {@link Sample}.
     * <p>
     * Changing this may mean that sorting no longer works on the server side if result set is used.
     * </p>
     */
    public static final String PROJECT_FOR_SAMPLE =
            EXPERIMENT_FOR_SAMPLE + FIELD_PATH_SEPARATOR + "project";

    public static final String REGISTRATION_DATE = "registrationDate";

    public static final String REGISTRATOR = "registrator";

    public static final String ROLE = "role";

    public static final String ROLES = "roles";

    public static final String SAMPLE_IDENTIFIER = "sampleIdentifier";

    public static final String SAMPLE_TYPE = "sampleType";

    public static final String EXPERIMENT_TYPE = "experimentType";

    public static final String USER_ID = "userId";

    public static final String PROJECT_WITH_GROUP = "projectWithGroup";

    public static final String PROJECT = "project";

    public static final String EXPERIMENT_IDENTIFIER = "experimentIdentifier";

    public static final String VERSIONS = "versions";

    public static final String VERSION_FILE_NAME = "versionsFileName";

    public static final String PROCEDURE = "procedure";

    public static final String MATERIAL_TYPE = "materialType";

    public static final String LABEL = "label";

    public static final String DATA_TYPE = "dataType";

    public static final String CONTROLLED_VOCABULARY = "controlledVocabulary";

    public static final String EXPERIMENT_TYPES = "experimentTypes";

    public static final String MATERIAL_TYPES = "materialTypes";

    public static final String SAMPLE_TYPES = "sampleTypes";

    private ModelDataPropertyNames()
    {
        // Can not be instantiated.
    }

}
