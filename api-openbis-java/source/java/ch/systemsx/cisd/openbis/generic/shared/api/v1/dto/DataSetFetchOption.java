/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FetchOption;

/**
 * @author pkupczyk
 */
@JsonObject("DataSetFetchOption")
public enum DataSetFetchOption implements FetchOption
{
    /** The basic attributes of a dataset like code, registration data, completeness, etc.. */
    BASIC,
    /** The experiment that the dataset is connected to. */
    EXPERIMENT,
    /** The sample that the dataset is connected to. */
    SAMPLE,
    /** The properties of a dataset. */
    PROPERTIES,
    /** The properties of a all material properties of a dataset. */
    PROPERTIES_OF_PROPERTIES,
    /** The properties of a all parent datasets of a dataset. */
    PROPERTIES_OF_PARENTS,
    /** The properties of a all child datasets of a dataset. */
    PROPERTIES_OF_CHILDREN,
    /** The parents of a dataset. */
    PARENTS,
    /** The children of a dataset. */
    CHILDREN,
    /** The containers of a dataset, if it is a contained dataset. */
    CONTAINER,
    /** The contained datasets of a dataset, if it is a container. */
    CONTAINED,
    /** The metaproject information of a dataset for the current user. */
    METAPROJECTS

}
