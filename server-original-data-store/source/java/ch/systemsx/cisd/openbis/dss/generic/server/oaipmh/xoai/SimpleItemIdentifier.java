/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.xoai;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.dspace.xoai.dataprovider.model.ItemIdentifier;
import org.dspace.xoai.dataprovider.model.Set;

/**
 * <p>
 * Simple implementation of {@link org.dspace.xoai.dataprovider.model.ItemIdentifier} with setters and getters.
 * </p>
 * 
 * @author pkupczyk
 */
public class SimpleItemIdentifier implements ItemIdentifier
{

    private String identifier;

    private Date datestamp;

    private List<Set> sets = new LinkedList<Set>();

    private boolean deleted;

    @Override
    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public Date getDatestamp()
    {
        return datestamp;
    }

    public void setDatestamp(Date datestamp)
    {
        this.datestamp = datestamp;
    }

    @Override
    public List<Set> getSets()
    {
        return sets;
    }

    public void setSets(List<Set> sets)
    {
        this.sets = sets;
    }

    @Override
    public boolean isDeleted()
    {
        return deleted;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

}
