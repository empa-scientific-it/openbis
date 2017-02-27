/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.exceptions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UnauthorizedObjectAccessExceptionTest
{

    @Test
    public void testMessageWithNoAbbrivatedIdList()
    {
        List<ObjectPermId> ids = Arrays.asList(new SamplePermId("123"), new ExperimentPermId("abc"));
        UnauthorizedObjectAccessException exception = new UnauthorizedObjectAccessException(ids);
        
        assertEquals(exception.getMessage(), "Access denied to at least one of the 2 = [123, ABC].");
        assertSame(exception.getObjectIds(), ids);
    }
    
    @Test
    public void testMessageWithAbbrivatedIdList()
    {
        List<ObjectPermId> ids = new ArrayList<>();
        for (int i = 0; i < 130; i++)
        {
            ids.add(new DataSetPermId(Integer.toString(i)));
        }
        UnauthorizedObjectAccessException exception = new UnauthorizedObjectAccessException(ids);
        
        assertEquals(exception.getMessage(), "Access denied to at least one of the 130 = [0, 1, 2, 3, 4, 5, 6, "
                + "7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, "
                + "31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, "
                + "54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, "
                + "77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, "
                + "99, ... (30 left)].");
        assertSame(exception.getObjectIds(), ids);
    }

}
