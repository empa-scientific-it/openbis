/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

/**
 * An enum listing the different kinds of entities that are searchable.
 * 
 * @author Piotr Buczek
 */
@JsonObject("SearchableEntityKind")
public enum SearchableEntityKind
{
    SAMPLE, EXPERIMENT, DATA_SET, MATERIAL,
    // sample subcriteria
    SAMPLE_CONTAINER, SAMPLE_PARENT, SAMPLE_CHILD,
    // data set subcriteria
    DATA_SET_CONTAINER, DATA_SET_PARENT, DATA_SET_CHILD
}