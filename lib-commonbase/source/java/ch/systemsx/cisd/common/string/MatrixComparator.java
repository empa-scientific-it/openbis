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

import java.io.Serializable;
import java.util.Comparator;

/**
 * A <code>Comparator</code> implementation that is based on matrix labelling having the following form: <code>[a-zA-Z]+[0-9]+</code>.
 * <p>
 * A default natural sorting will place, for instance, <code>A3</code> before <code>A10</code>. This comparator can also sort by letter first or by
 * number first.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class MatrixComparator implements Comparator<String>, Serializable
{

    private static final long serialVersionUID = 1L;

    private final boolean letterFirst;

    public MatrixComparator()
    {
        this(true);
    }

    public MatrixComparator(final boolean letterFirst)
    {
        this.letterFirst = letterFirst;
    }

    @Override
    public final int compare(String o1, String o2)
    {
        final String[] s1 = StringUtilities.splitMatrixCoordinate(o1);
        final String[] s2 = StringUtilities.splitMatrixCoordinate(o2);
        if (s1 != null && s2 != null)
        {
            int sCompare = s1[0].compareTo(s2[0]);
            // No check for NumberFormatException as we are sure we have numbers here.
            int iCompare = Integer.parseInt(s1[1]) - Integer.parseInt(s2[1]);
            if (letterFirst)
            {
                return sCompare == 0 ? iCompare : sCompare;
            } else
            {
                return iCompare == 0 ? sCompare : iCompare;
            }
        }
        return o1.compareTo(o2);
    }
}