/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.string;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.string.MatrixComparator;

/**
 * Test cases for the {@link MatrixComparator}.
 * 
 * @author Christian Ribeaud
 */
public final class MatrixComparatorTest
{

    private final static String[] createData()
    {
        return new String[]
        { "a10", "a3", "A11", "B4", "C6", "c23", "a1", "a01" };
    }

    @Test
    public final void testLetterFirst()
    {
        String[] s = createData();
        Arrays.sort(s, new MatrixComparator());
        assert Arrays.equals(s, new String[]
        { "A11", "B4", "C6", "a1", "a01", "a3", "a10", "c23" });
    }

    @Test
    public final void testDigitFirst()
    {
        String[] s = createData();
        Arrays.sort(s, new MatrixComparator(false));
        assert Arrays.equals(s, new String[]
        { "a1", "a01", "a3", "B4", "C6", "a10", "A11", "c23" });
    }

    @Test
    public final void testWrongPattern()
    {
        String[] s =
        { "12", "3", "aa", "bb", "1c" };
        String[] clone = (String[]) ArrayUtils.clone(s);
        Arrays.sort(s, new MatrixComparator());
        Arrays.sort(clone);
        Arrays.equals(s, clone);
    }
}