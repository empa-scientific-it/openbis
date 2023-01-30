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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;

/**
 * @author Piotr Buczek
 */
public class ComparableCellValueHelper
{

    // TODO 2010-03-10, CR: This code should be a method on ISerializableComparable and implemented
    // using polymorphism.
    public static Comparable<?> unwrap(ISerializableComparable cellValue)
    {
        if (cellValue instanceof DateTableCell)
        {
            return ((DateTableCell) cellValue).getDateTime();
        } else if (cellValue instanceof DoubleTableCell)
        {
            return ((DoubleTableCell) cellValue).getNumber();
        } else if (cellValue instanceof IntegerTableCell)
        {
            return ((IntegerTableCell) cellValue).getNumber();
        } else
        {
            return cellValue.toString();
        }
    }

}
