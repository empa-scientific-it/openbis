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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;

/**
 * A bean that contains information about a sample's derived samples and data sets.
 * 
 * @author Ganime Akin
 */
@DoNotEscape
public final class SampleChildrenInfo implements IsSerializable
{
    private String sampleIdentifier;

    private List<String> derivedSamples = new ArrayList<String>();

    private List<String> dataSets = new ArrayList<String>();

    // we need the following values in case we want to send
    // only children count info, not the lists above
    private int childCount;

    private int dataSetCount;

    public int getChildCount()
    {
        return childCount;
    }

    public void setChildCount(int childCount)
    {
        this.childCount = childCount;
    }

    public int getDataSetCount()
    {
        return dataSetCount;
    }

    public void setDataSetCount(int dataSetCount)
    {
        this.dataSetCount = dataSetCount;
    }

    public void addDerivedSample(String s)
    {
        this.derivedSamples.add(s);
    }

    public void addDataSet(String s)
    {
        this.dataSets.add(s);
    }

    public SampleChildrenInfo(String sampleIdentifier)
    {
        super();
        this.sampleIdentifier = sampleIdentifier;
    }

    public String getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleIdentifier(String sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    public List<String> getDerivedSamples()
    {
        return derivedSamples;
    }

    public List<String> getDataSets()
    {
        return dataSets;
    }

    @SuppressWarnings("unused")
    // GWT only
    private SampleChildrenInfo()
    {
    }
}
