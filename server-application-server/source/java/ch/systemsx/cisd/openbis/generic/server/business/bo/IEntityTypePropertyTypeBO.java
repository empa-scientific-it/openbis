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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;

/**
 * Business Object dealing with entity type - property type relations.
 * 
 * @author Izabela Adamczyk
 */
public interface IEntityTypePropertyTypeBO
{

    /**
     * Create a new Entity Type - Property Type relation.
     */
    void createAssignment(NewETPTAssignment newAssignment);

    /**
     * Loads assignments between specified property type and entity type.
     */
    void loadAssignment(String propertyTypeCode, String entityTypeCode);

    /**
     * Returns number of property values used by entities for assignment between specified property type and entity type.
     */
    int countAssignmentValues(String propertyTypeCode, String entityTypeCode);

    /**
     * Returns loaded assignment.
     */
    EntityTypePropertyTypePE getLoadedAssignment();

    /**
     * Deletes loaded assignment. Does nothing if no assignment loaded.
     */
    void deleteLoadedAssignment();

    /**
     * Updates loaded assignment. Does nothing if no assignment loaded.
     */
    void updateLoadedAssignment(NewETPTAssignment assignmentUpdates);

}
