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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application;

/**
 * An {@link ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict} extension for <i>query</i> specific message keys.
 * 
 * @author Christian Ribeaud
 */
public final class Dict extends ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict
{
    public static final String QUERY_MODULE_MENU_TITLE = "query_module_menu_title";

    public static final String QUERY_DATABASE_MENU_TITLE = "query_database_menu_title";

    public static final String BUTTON_ADD_QUERY = "button_add_query";

    public static final String SQL_QUERY = "sql_query";

    public static final String BUTTON_TEST_QUERY = "button_test_query";

    public static final String QUERY = "query";

    public static final String QUERY_EXECUTE = "query_execute";

    public static final String QUERY_DELETION_CONFIRMATION = "query_deletion_confirmation";

    public static final String QUERY_CREATE_TITLE = "query_create_title";

    public static final String QUERY_EDIT_TITLE = "query_edit_title";

    public static final String QUERY_PARAMETERS_BINDINGS_DIALOG_TITLE =
            "query_parameters_binding_dialog_title";

    public static final String QUERY_TYPE = "query_type";

    public static final String QUERY_DATABASE = "query_database";

    private Dict()
    {
        // Can not be instantiated.
    }
}
