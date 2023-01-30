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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

public final class ExperimentIdentifier implements IsSerializable
{
    private String identifier;

    public static ExperimentIdentifier createIdentifier(Experiment entity)
    {
        return new ExperimentIdentifier(entity.getIdentifier());
    }

    public ExperimentIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    // GWT only
    @SuppressWarnings("unused")
    private ExperimentIdentifier()
    {
    }

    public String getIdentifier()
    {
        return identifier;
    }

    @Override
    public String toString()
    {
        return "Experiment[" + identifier + "]";
    }

}
