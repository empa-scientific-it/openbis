/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;

/**
 * A {@link DataType} &lt;---&gt; {@link DataTypePE} translator.
 * 
 * @author Izabela Adamczyk
 */
public class DataTypeTranslator
{
    private DataTypeTranslator()
    {
        // Can not be instantiated.
    }

    public static DataType translate(final DataTypePE dataTypePE)

    {
        final DataType result = new DataType();
        result.setCode(dataTypePE.getCode());
        result.setDescription(dataTypePE.getDescription());
        return result;
    }

    public static List<DataType> translate(List<DataTypePE> dataTypePEs)
    {
        List<DataType> result = new ArrayList<DataType>();
        for (DataTypePE dt : dataTypePEs)
        {
            result.add(translate(dt));
        }
        return result;
    }

}
