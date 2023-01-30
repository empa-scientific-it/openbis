/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.OperationKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;

/**
 * @author Franz-Josef Elmer
 */
public class SearchObjectKindModificationTest extends AbstractTest
{
    @Test
    public void testSearchForAllObjectKindModifications()
    {
        ObjectKindModificationSearchCriteria searchCriteria = new ObjectKindModificationSearchCriteria();
        ObjectKindModificationFetchOptions fetchOptions = new ObjectKindModificationFetchOptions();

        SearchResult<ObjectKindModification> searchResult = v3api.searchObjectKindModifications(systemSessionToken, searchCriteria, fetchOptions);

        assertEquals(searchResult.getTotalCount(), ObjectKind.values().length * OperationKind.values().length);
    }

    @Test
    public void testSearchForProjectModifications()
    {
        sleep(100);
        Date date = new Date();
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        projectUpdate.setDescription("time stamp: " + date);
        v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate));
        ObjectKindModificationSearchCriteria searchCriteria = new ObjectKindModificationSearchCriteria();
        searchCriteria.withObjectKind().thatIn(ObjectKind.PROJECT, ObjectKind.SAMPLE);
        searchCriteria.withOperationKind().thatIn(OperationKind.UPDATE);
        ObjectKindModificationFetchOptions fetchOptions = new ObjectKindModificationFetchOptions();

        SearchResult<ObjectKindModification> searchResult = v3api.searchObjectKindModifications(systemSessionToken, searchCriteria, fetchOptions);

        assertEquals(searchResult.getTotalCount(), 2);
        List<ObjectKindModification> modifications = searchResult.getObjects();
        for (ObjectKindModification modification : modifications)
        {
            Date lastModificationTimeStamp = modification.getLastModificationTimeStamp();
            ObjectKind objectKind = modification.getObjectKind();
            if (objectKind == ObjectKind.SAMPLE)
            {
                assertTrue(lastModificationTimeStamp.getTime() < date.getTime(),
                        "Expected date (" + date + ") >= " + modification);
            } else if (objectKind == ObjectKind.PROJECT)
            {
                assertTrue(lastModificationTimeStamp.getTime() >= date.getTime(),
                        "Expected date (" + date + ") < " + modification);
            } else
            {
                fail("Unexpect object kind: " + objectKind);
            }
            assertEquals(modification.getOperationKind(), OperationKind.UPDATE);
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ObjectKindModificationSearchCriteria c = new ObjectKindModificationSearchCriteria();
        c.withObjectKind().thatIn(ObjectKind.SAMPLE, ObjectKind.SAMPLE_TYPE);

        ObjectKindModificationFetchOptions fo = new ObjectKindModificationFetchOptions();

        v3api.searchObjectKindModifications(sessionToken, c, fo);

        assertAccessLog(
                "search-object-kind-modifications  SEARCH_CRITERIA:\n'OBJECT_KIND_MODIFICATION_SEARCH\n    with object kinds [SAMPLE, SAMPLE_TYPE]\n'\nFETCH_OPTIONS:\n'ObjectKindModification\n'");
    }

}
