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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;

/**
 * Common {@link IAttributeSearchFieldKind} provider used both on client and server side.
 * 
 * @author Piotr Buczek
 */
public class AttributeSearchFieldKindProvider
{

    public static IAttributeSearchFieldKind[] getAllAttributeFieldKinds(EntityKind entityKind)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return DataSetAttributeSearchFieldKind.values();
            case EXPERIMENT:
                return ExperimentAttributeSearchFieldKind.values();
            case MATERIAL:
                return MaterialAttributeSearchFieldKind.values();
            case SAMPLE:
                return SampleAttributeSearchFieldKind.values();
        }
        return null; // cannot happen
    }

    /**
     * Return the attribute search field kind that matches the given entityKind / code combintation
     */
    public static IAttributeSearchFieldKind getAttributeFieldKind(EntityKind entityKind, String code)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return DataSetAttributeSearchFieldKind.valueOf(code);
            case EXPERIMENT:
                return ExperimentAttributeSearchFieldKind.valueOf(code);
            case MATERIAL:
                return MaterialAttributeSearchFieldKind.valueOf(code);
            case SAMPLE:
                return SampleAttributeSearchFieldKind.valueOf(code);
        }
        return null; // cannot happen
    }
}
