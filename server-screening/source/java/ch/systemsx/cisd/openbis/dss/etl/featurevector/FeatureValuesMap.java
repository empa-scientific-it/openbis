/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Stores values of one feature for all wells (and optionally one chosen timepoint and/or depth-scan).
 * 
 * @author Tomasz Pylak
 */
public class FeatureValuesMap implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Map<WellLocation, String> valuesMap;

    private final Double depthOrNull;

    private final Double timeOrNull;

    public FeatureValuesMap(Double timeOrNull, Double depthOrNull)
    {
        this.depthOrNull = depthOrNull;
        this.timeOrNull = timeOrNull;
        this.valuesMap = new HashMap<WellLocation, String>();
    }

    public Double tryGetDepth()
    {
        return depthOrNull;
    }

    public Double tryGetTime()
    {
        return timeOrNull;
    }

    /** Add a value for one well. */
    public void addValue(String value, WellLocation wellPos)
    {
        if (valuesMap.get(wellPos) != null)
        {
            throw new IllegalStateException("Value for the well " + wellPos
                    + " has been already defined!");
        }
        valuesMap.put(wellPos, value);
    }

    /** @return set of unique values of this feature (makes sense only for vocabulary terms). */
    public Set<String> getUniqueAvailableValues()
    {
        Set<String> uniqueValues = new HashSet<String>();
        for (String value : valuesMap.values())
        {
            uniqueValues.add(value);
        }
        return uniqueValues;
    }

    /**
     * Tries to parse all values as float numbers.
     * 
     * @return null if any column value cannot be parsed as float number.
     */
    public Map<WellLocation, Float> tryExtractFloatValues()
    {
        Map<WellLocation, Float> map = new HashMap<WellLocation, Float>();
        for (Entry<WellLocation, String> entry : valuesMap.entrySet())
        {
            try
            {
                WellLocation wellLocation = entry.getKey();
                String value = entry.getValue();
                float floatValue = parseFloatOrEmptyString(value);
                map.put(wellLocation, floatValue);
            } catch (NumberFormatException ex)
            {
                return null;
            }
        }
        return map;
    }

    private float parseFloatOrEmptyString(String value)
    {
        float floatValue;
        if (StringUtils.isBlank(value))
        {
            floatValue = Float.NaN;
        } else
        {
            floatValue = Float.parseFloat(value);
        }
        return floatValue;
    }

    /**
     * Assuming that all values come from the set fixed of vocabulary terms calculates the mapping from vocabulary term into a unique term sequence
     * number.<br>
     * Should be called when {@link #tryExtractFloatValues} returns null.
     * 
     * @return mapping between wells and integer sequence numbers of terms casted to floats
     */
    public Map<WellLocation, Float> calculateWellTermsMapping(
            Map<String, Integer> valueToSequanceMap)
    {
        Map<WellLocation, Float> wellTermsMapping = new HashMap<WellLocation, Float>();
        for (Entry<WellLocation, String> entry : valuesMap.entrySet())
        {
            WellLocation wellLocation = entry.getKey();
            String value = entry.getValue();
            int sequenceNumber = valueToSequanceMap.get(value);
            wellTermsMapping.put(wellLocation, (float) sequenceNumber);
        }
        return wellTermsMapping;
    }

    public boolean isEmpty()
    {
        return valuesMap.isEmpty();
    }

    public void validate(Geometry plateGeometry)
    {
        for (WellLocation well : valuesMap.keySet())
        {
            validate(well, plateGeometry);
        }
    }

    private static void validate(WellLocation wellPos, Geometry plateGeometry)
    {
        if (wellPos.getRow() > plateGeometry.getNumberOfRows()
                || wellPos.getColumn() > plateGeometry.getNumberOfColumns())
        {
            throw new IllegalStateException(
                    String.format(
                            "Feature value if defined for the well '%s' which is outside of the plate matrix '%s'",
                            wellPos, plateGeometry));
        }
    }
}
