/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.uitest.dsl.IdentifiedBy;

/**
 * @author anttil
 */
public class Header
{
    private SampleType type;

    private List<String> labels;

    public Header(SampleType type, boolean hasContainerColumn)
    {
        this.type = type;

        this.labels = new ArrayList<String>();

        if (type.isGenerateCodes() == false)
        {
            labels.add("Identifier");
        }

        if (type.isShowContainer() && hasContainerColumn)
        {
            labels.add("CURRENT_CONTAINER");
        }
        for (PropertyTypeAssignment assignment : type.getPropertyTypeAssignments())
        {
            labels.add(assignment.getPropertyType().getCode());
        }
    }

    public List<String> getLabels()
    {
        return labels;
    }

    public List<String> getValuesFor(Sample sample, Map<Sample, IdentifiedBy> idMap)
    {
        List<String> values = new ArrayList<String>();
        for (String label : labels)
        {
            if (label.equals("Identifier"))
            {
                values.add(getIdentifier(sample, idMap));
            }
            else if (label.equals("CURRENT_CONTAINER"))
            {
                if (sample.getContainer() != null)
                {
                    values.add(getIdentifier(sample.getContainer(), idMap));
                } else
                {
                    values.add("");
                }
            } else
            {
                PropertyType key = null;
                for (PropertyTypeAssignment assignment : type.getPropertyTypeAssignments())
                {
                    if (assignment.getPropertyType().getCode().equals(label))
                    {
                        key = assignment.getPropertyType();
                        break;
                    }
                }

                if (key == null)
                {
                    throw new IllegalArgumentException("cannot find property type " + label
                            + " from " + type.getPropertyTypeAssignments());
                }

                Object value = sample.getProperties().get(key);
                if (value != null)
                {
                    values.add(value.toString());
                } else
                {
                    values.add("");
                }
            }
        }
        return values;
    }

    private String getIdentifier(Sample sample, Map<Sample, IdentifiedBy> identifierTypes)
    {

        if (sample.getCode() == null || sample.getCode().isEmpty())
        {
            if (sample.getType().isGenerateCodes())
            {
                return "";
            } else
            {
                throw new IllegalArgumentException("Sample code cannot be empty");
            }
        }

        IdentifiedBy idType = identifierTypes.get(sample);
        if (idType == null)
        {
            idType = IdentifiedBy.SPACE_AND_CODE;
        }

        return idType.format(sample);
    }

}
