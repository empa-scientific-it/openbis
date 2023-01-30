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

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * Stores one primitive value: Double, Long or String (null is represented as "" - {@link PrimitiveValue#NULL}).
 * <p>
 * Such a type is needed because GWT does not support serialization fields of Object or Serializable type.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class PrimitiveValue implements IsSerializable, Serializable, Comparable<PrimitiveValue>
{
    private static final long serialVersionUID = 1L;

    public static PrimitiveValue NULL = new PrimitiveValue("");

    private Double doubleValueOrNull;

    private Long longValueOrNull;

    private String stringValueOrNull;

    private DataTypeCode dataTypeCodeOrNull;

    public PrimitiveValue(Double value)
    {
        assert value != null;
        doubleValueOrNull = value;
        dataTypeCodeOrNull = DataTypeCode.REAL;
    }

    public PrimitiveValue(Long value)
    {
        assert value != null;
        longValueOrNull = value;
        dataTypeCodeOrNull = DataTypeCode.INTEGER;
    }

    public PrimitiveValue(String value)
    {
        assert value != null;
        stringValueOrNull = value;
        dataTypeCodeOrNull = DataTypeCode.VARCHAR;
    }

    public DataTypeCode getDataType()
    {
        return dataTypeCodeOrNull;
    }

    @Override
    public String toString()
    {
        if (doubleValueOrNull != null)
        {
            return doubleValueOrNull.toString();
        } else if (longValueOrNull != null)
        {
            return longValueOrNull.toString();
        } else
        {
            return stringValueOrNull;
        }
    }

    @Override
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public int compareTo(PrimitiveValue o)
    {
        Integer thisTypeOrdinal = getComparableDataTypeOrdinal();
        Integer thatTypeOrdinal = o.getComparableDataTypeOrdinal();
        int typeComparisonResult = thisTypeOrdinal.compareTo(thatTypeOrdinal);
        if (typeComparisonResult != 0)
        {
            return typeComparisonResult;
        } else
        {
            Comparable v1 = getComparableValue();
            Comparable v2 = o.getComparableValue();
            return v1.compareTo(v2);
        }
    }

    // Exposed for testing
    public Comparable<?> getComparableValue()
    {
        if (doubleValueOrNull != null)
        {
            return doubleValueOrNull;
        } else if (longValueOrNull != null)
        {
            return Double.valueOf(longValueOrNull);
        } else
        {
            return stringValueOrNull != null ? stringValueOrNull : "";
        }
    }

    private Integer getComparableDataTypeOrdinal()
    {
        // strings should be always smaller compared to integers and reals
        switch (getDataType())
        {
            case INTEGER:
            case REAL:
                return 1;
            default:
                return 0;
        }
    }

    @SuppressWarnings("unused")
    private PrimitiveValue()
    {
    }

}
