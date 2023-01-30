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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * Criteria for tracking <i>data sets</i> with technical id bigger than the specified one. Optional, the search is restricted to data sets connected
 * to samples of a certain type.
 * <p>
 * Connected samples should be loaded as well as their parent and container samples according to {@link SampleType} hierarchy depths. All referenced
 * samples should have all properties loaded.
 * 
 * @author Piotr Buczek
 */
public class TrackingDataSetCriteria implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final String connectedSampleTypeCode;

    private final long lastSeenDataSetId;

    private final boolean enrichResult;

    public TrackingDataSetCriteria(long lastSeenDataSetId)
    {
        this(null, lastSeenDataSetId, false);
    }

    public TrackingDataSetCriteria(String connectedSampleTypeCodeOrNull, long lastSeenDataSetId)
    {
        this(connectedSampleTypeCodeOrNull, lastSeenDataSetId, true);
        assert connectedSampleTypeCodeOrNull != null;
    }

    private TrackingDataSetCriteria(String connectedSampleTypeCodeOrNull, long lastSeenDataSetId,
            boolean enrichResult)
    {
        this.enrichResult = enrichResult;
        this.lastSeenDataSetId = lastSeenDataSetId;
        this.connectedSampleTypeCode = connectedSampleTypeCodeOrNull;
    }

    public String getConnectedSampleTypeCode()
    {
        return connectedSampleTypeCode;
    }

    public long getLastSeenDataSetId()
    {
        return lastSeenDataSetId;
    }

    public boolean shouldResultBeEnriched()
    {
        return enrichResult;
    }

    @Override
    public String toString()
    {
        return "lastSeenDataSetId:"
                + lastSeenDataSetId
                + (connectedSampleTypeCode == null ? "" : " sample type:" + connectedSampleTypeCode)
                + (enrichResult ? " (enrich)" : "");
    }

}
