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

import java.util.LinkedList;
import java.util.List;

import org.dspace.xoai.dataprovider.model.Item;
import org.dspace.xoai.model.oaipmh.About;
import org.dspace.xoai.model.oaipmh.Metadata;

/**
 * <p>
 * Simple implementation of {@link org.dspace.xoai.dataprovider.model.Item} with setters and getters.
 * </p>
 * 
 * @author pkupczyk
 */
public class SimpleItem extends SimpleItemIdentifier implements Item
{

    private List<About> about = new LinkedList<About>();

    private Metadata metadata;

    @Override
    public List<About> getAbout()
    {
        return about;
    }

    public void setAbout(List<About> about)
    {
        this.about = about;
    }

    @Override
    public Metadata getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Metadata metadata)
    {
        this.metadata = metadata;
    }

}
