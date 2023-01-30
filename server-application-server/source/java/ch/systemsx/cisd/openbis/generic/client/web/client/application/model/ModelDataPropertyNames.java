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

/**
 * Some constants used in {@link ModelData} implementations. These constants are typically used in {@link ColumnConfig#setId(String)}. Because they
 * serve a different purpose, they should not be used in {@link ColumnConfig#setHeader(String)}.
 * <p>
 * Use <i>Java</i> coding standard for naming these property names and be aware that some of them could be use for sorting when using <i>Result
 * Set</i>.
 * </p>
 * <p>
 * <b>Important note</b>: Do not put a <code>_</code> in the property name except for specifying a field path.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ModelDataPropertyNames
{
    /**
     * Try to avoid using this class! Define your model keys as private constants in the model and expose them with getters.
     */

    public static final String CODE = "code";

    public static final String CODE_WITH_LABEL = "code_with_label";

    public static final String TOOLTIP = "tooltip";

    public static final String LABEL = "label";

    public static final String DATA_SET_TYPES = "data_set_types";

    public static final String DESCRIPTION = "description";

    public static final String OBJECT = "object";

    public static final String REGISTRATION_DATE = "registrationDate";

    public static final String REGISTRATOR = "registrator";

    public static final String PROJECT_IDENTIFIER = "projectIdentifier";

    public static final String NAME = "name";

    private ModelDataPropertyNames()
    {
        // Can not be instantiated.
    }

    public static String link(String columnId)
    {
        return columnId + "__href";
    }

}
