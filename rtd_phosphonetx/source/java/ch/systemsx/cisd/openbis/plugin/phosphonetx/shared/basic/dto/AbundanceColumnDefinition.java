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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AbundanceColumnDefinition implements Serializable, IsSerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
    
    private long sampleID;
    
    private String sampleCode;
    
    private List<Treatment> treatments;
    
    public final long getSampleID()
    {
        return sampleID;
    }

    public final void setSampleID(long sampleID)
    {
        this.sampleID = sampleID;
    }

    public final String getSampleCode()
    {
        return sampleCode;
    }

    public final void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public final List<Treatment> getTreatments()
    {
        return treatments;
    }

    public final void setTreatments(List<Treatment> treatments)
    {
        this.treatments = treatments;
    }
}
