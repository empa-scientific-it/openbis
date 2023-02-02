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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * A holder class for analysis procedures. The codes list contains unique values. It can contain NULL (which can be used for data sets having no
 * ANALYSIS_PROCEDURE value specified).
 * 
 * @author Kaloyan Enimanev
 */
public class AnalysisProcedures implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<String> procedureCodes;

    // GWT only
    @SuppressWarnings("unused")
    private AnalysisProcedures()
    {
    }

    public AnalysisProcedures(Set<String> procedureCodes)
    {
        this.procedureCodes = new ArrayList<String>(procedureCodes);
    }

    public List<String> getProcedureCodes()
    {
        return Collections.unmodifiableList(procedureCodes);
    }

}
