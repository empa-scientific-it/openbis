/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

/**
 * Prefixes for indexed fields. Should be used everywhere where {@link IndexedEmbedded} or {@link Field} annotation is used, in this way we can make
 * search query syntax independent from bean field names.
 * 
 * @author Tomasz Pylak
 */
public final class SearchFieldConstants
{
    private static final String SEPARATOR = " ";

    public static final String PREFIX_PROPERTIES = "property" + SEPARATOR;

    public static final String PREFIX_EXPERIMENT = "experiment" + SEPARATOR;

    public static final String PREFIX_SAMPLE = "sample" + SEPARATOR;

    public static final String PREFIX_CONTAINER = "container" + SEPARATOR;

    public static final String PREFIX_ENTITY_TYPE = "type" + SEPARATOR;

    public static final String PREFIX_PROJECT = "project" + SEPARATOR;

    public static final String PREFIX_SPACE = "space" + SEPARATOR;

    public static final String PREFIX_REGISTRATOR = "registrator" + SEPARATOR;

    public static final String PREFIX_MODIFIER = "modifier" + SEPARATOR;

    public static final String PREFIX_ATTACHMENT = "attachment" + SEPARATOR;

    public static final String PREFIX_VOCABULARY_TERM = "vocabulary term" + SEPARATOR;

    public static final String PREFIX_METAPROJECT = "metaproject" + SEPARATOR;

    public static final String PREFIX_FILE_FORMAT_TYPE = "file format type" + SEPARATOR;

    public static final String PREFIX_LOCATOR_TYPE = "locator type" + SEPARATOR;

    public static final String PREFIX_STORAGE_FORMAT = "storage format" + SEPARATOR;

    public static final String PREFIX_EXTERNAL_DMS = "external dms" + SEPARATOR;

    public static final String PREFIX_CONTENT_COPY = "content copy" + SEPARATOR;

    public static final String ID = "id";

    public static final String EXPERIMENT_ID = PREFIX_EXPERIMENT + ID;

    public static final String PROJECT_ID = PREFIX_PROJECT + ID;

    public static final String SAMPLE_ID = PREFIX_SAMPLE + ID;

    public static final String CONTAINER_ID = PREFIX_CONTAINER + ID;

    public static final String SPACE_ID = PREFIX_SPACE + ID;

    public static final String SAMPLE_SPACE_ID = PREFIX_SAMPLE + PREFIX_SPACE + ID;

    public static final String DELETED = "deleted";

    public static final String PERSON_LAST_NAME = "Last Name";

    public static final String PERSON_FIRST_NAME = "First Name";

    public static final String PERSON_EMAIL = "Email";

    public static final String PERSON_USER_ID = "User Id";

    public static final String CODE = "code";

    public static final String PERM_ID = "perm_id";

    public static final String IDENTIFIER = "identifier";

    public static final String FILE_NAME = "name";

    public static final String FILE_TITLE = "title";

    public static final String FILE_DESCRIPTION = "description";

    public static final String STORAGE_CONFIRMATION = "storage_confirmed";

    public static final String SHARE_ID = "share_id";

    public static final String LOCATION = "location";

    public static final String SIZE = "size";

    public static final String COMPLETE = "complete";

    public static final String STATUS = "status";

    public static final String PRESENT_IN_ARCHIVE = "present_in_archive";

    public static final String ARCHIVING_REQUESTED = "archiving_requested";
    
    public static final String SPEED_HINT = "speed_hint";

    public static final String EXTERNAL_CODE = "external_code";

    public static final String REGISTRATION_DATE = "registration_date";

    public static final String MODIFICATION_DATE = "modification_date";

    public static final String ACCESS_DATE = "access_date";

    public static final String IS_LISTABLE = "is_listable";

    public static final String ADDRESS = "address";

    public static final String ADDRESS_TYPE = "address_type";

    public static final String LABEL = "label";

    public static final String PATH = "path";

    public static final String GIT_COMMIT_HASH = "git_commit_hash";

    public static final String GIT_REPOSITORY_ID = "git_repository_id";
}
