/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;

/**
 * Information about a share.
 *
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("ShareInfo")
public class ShareInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String shareId;

    private long freeSpace;

    private boolean incoming;

    private boolean withdrawShare;

    private boolean ignoredForShuffling;

    public ShareInfo(String shareId, long freeSpace)
    {
        this.shareId = shareId;
        this.freeSpace = freeSpace;
    }

    public String getShareId()
    {
        return shareId;
    }

    @JsonIgnore
    public long getFreeSpace()
    {
        return freeSpace;
    }

    /**
     * Returns <code>true</code> if the share is associated with one or more incoming directories.
     */
    public boolean isIncoming()
    {
        return incoming;
    }

    public void setIncoming(boolean incoming)
    {
        this.incoming = incoming;
    }

    /**
     * Returns <code>true</code> if the share should be emptied by shuffling maintenance tasks.
     */
    public boolean isWithdrawShare()
    {
        return withdrawShare;
    }

    public void setWithdrawShare(boolean withdrawShare)
    {
        this.withdrawShare = withdrawShare;
    }

    /**
     * Returns <code>true</code> if the share should not be used by shuffling maintenance or post-registration tasks.
     */
    public boolean isIgnoredForShuffling()
    {
        return ignoredForShuffling;
    }

    public void setIgnoredForShuffling(boolean ignoredForShuffling)
    {
        this.ignoredForShuffling = ignoredForShuffling;
    }

    //
    // JSON-RPC
    //

    private ShareInfo()
    {
    }

    private void setShareId(String shareId)
    {
        this.shareId = shareId;
    }

    @JsonIgnore
    private void setFreeSpace(long freeSpace)
    {
        this.freeSpace = freeSpace;
    }

    @JsonProperty("freeSpace")
    private String getFreeSpaceAsString()
    {
        return JsonPropertyUtil.toStringOrNull(freeSpace);
    }

    private void setFreeSpaceAsString(String freeSpace)
    {
        this.freeSpace = JsonPropertyUtil.toLongOrNull(freeSpace);
    }

}
