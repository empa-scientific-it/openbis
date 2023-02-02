/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.test;

import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;

/**
 * @author Franz-Josef Elmer
 */
public class PlateUtilsTest extends AssertJUnit
{
    @Test
    public void test()
    {
        List<String> codes = new ArrayList<String>();
        for (int i = 1; i < 60; i++)
        {
            codes.add(PlateUtils.translateRowNumberIntoLetterCode(i));
        }
        assertEquals("[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, "
                + "U, V, W, X, Y, Z, AA, AB, AC, AD, AE, AF, AG, AH, AI, AJ, AK, AL, "
                + "AM, AN, AO, AP, AQ, AR, AS, AT, AU, AV, AW, AX, AY, AZ, "
                + "BA, BB, BC, BD, BE, BF, BG]", codes.toString());
    }
}
