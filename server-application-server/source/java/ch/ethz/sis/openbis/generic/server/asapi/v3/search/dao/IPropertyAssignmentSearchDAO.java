/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao;

import java.util.Set;

/**
 * Data access interface for ad hoc functionality for assignments.
 */
public interface IPropertyAssignmentSearchDAO
{

    /**
     * Ad doc method for searching assignments with no annotations.
     * <p/>
     * Since no user rights filtering is needed this can be done in one query.
     *
     * @return search result
     */
    Set<Long> findAssignmentsWithoutAnnotations(final Set<Long> semanticAnnotationsPropertyIds,
            final String idsColumnName);

}
