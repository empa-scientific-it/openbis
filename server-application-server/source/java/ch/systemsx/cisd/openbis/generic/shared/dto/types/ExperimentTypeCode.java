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

package ch.systemsx.cisd.openbis.generic.shared.dto.types;

/**
 * The current <code>ExperimentType</code> codes.
 * <p>
 * This enumeration should reflect the values in the database and is <i>Unit</i> tested to ensure this point.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public enum ExperimentTypeCode
{
    SIRNA_HCS("SIRNA_HCS"), COMPOUND_HCS("COMPOUND_HCS");

    private final String code;

    private ExperimentTypeCode(final String code)
    {
        this.code = code;
    }

    public final String getCode()
    {
        return code;
    }

    /** For given <var>typeCode</var> returns the corresponding <code>ExperimentTypeCode</code>. */
    public final static ExperimentTypeCode getExperimentTypeCode(final String typeCode)
    {
        assert typeCode != null : "Unspecified experiment type code.";
        for (final ExperimentTypeCode experimentTypeCode : values())
        {
            if (experimentTypeCode.code.equalsIgnoreCase(typeCode))
            {
                return experimentTypeCode;
            }
        }
        throw new IllegalArgumentException(String.format("No experiment type for given code '%s'.",
                typeCode));
    }

}
