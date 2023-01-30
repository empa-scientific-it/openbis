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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * Parameters for formating real numbers in grids. It is part of {@link DisplaySettings}.
 * 
 * @author Franz-Josef Elmer
 */
public class RealNumberFormatingParameters implements Serializable
{
    private static final long serialVersionUID = 1L;

    // WORKAROUND 2010-09-13, IA: Used to avoid problems with GWT NumberFormat (should go away after
    // upgrade to GWT 2.1)
    public static int MAX_PRECISION = 6;

    private boolean formatingEnabled = true;

    private int precision = 4;

    private boolean scientific;

    public boolean isFormatingEnabled()
    {
        return formatingEnabled;
    }

    public void setFormatingEnabled(boolean formatingEnabled)
    {
        this.formatingEnabled = formatingEnabled;
    }

    public int getPrecision()
    {
        return precision > MAX_PRECISION ? MAX_PRECISION : precision;
    }

    public void setPrecision(int precision)
    {
        this.precision = precision;
    }

    public boolean isScientific()
    {
        return scientific;
    }

    public void setScientific(boolean scientific)
    {
        this.scientific = scientific;
    }
}
