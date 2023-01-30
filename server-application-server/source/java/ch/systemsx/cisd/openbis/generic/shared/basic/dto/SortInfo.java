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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Aggregates sort field and sort direction.
 * 
 * @author Christian Ribeaud
 */
public final class SortInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String sortField;

    private SortDir sortDir = SortDir.NONE;

    public final String getSortField()
    {
        return sortField;
    }

    public final void setSortField(final String sortField)
    {
        this.sortField = sortField;
    }

    public final SortDir getSortDir()
    {
        return sortDir;
    }

    public final void setSortDir(final SortDir sortDir)
    {
        this.sortDir = sortDir;
    }

    //
    // Helper classes
    //

    /**
     * Sort direction enumeration.
     * 
     * @author Christian Ribeaud
     */
    public static enum SortDir implements IsSerializable
    {
        NONE, ASC, DESC;
    }
}
