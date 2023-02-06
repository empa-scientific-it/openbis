/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.OperationKind;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.objectkindmodification.search.OperationKindCriteria")
public class OperationKindCriteria extends AbstractSearchCriteria
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private List<OperationKind> operationKinds;

    public void thatIn(List<OperationKind> kinds)
    {
        this.operationKinds = kinds;
    }

    public void thatIn(OperationKind... kinds)
    {
        this.operationKinds = Arrays.asList(kinds);
    }

    public List<OperationKind> getOperationKinds()
    {
        return operationKinds;
    }

    @Override
    public String toString()
    {
        return "with operation kinds " + operationKinds;
    }
}
