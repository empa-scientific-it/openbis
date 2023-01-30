/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * @author Pawel Glyzewski
 */
@Entity
@Table(name = TableNames.LINK_DATA_TABLE)
@PrimaryKeyJoinColumn(name = ColumnNames.ID_COLUMN)
public class LinkDataPE extends DataPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private Set<ContentCopyPE> contentCopies;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "dataSet", orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    public Set<ContentCopyPE> getContentCopies()
    {
        return contentCopies;
    }

    public void setContentCopies(final Set<ContentCopyPE> contentCopies)
    {
        this.contentCopies = contentCopies;
    }

    @Override
    @Transient
    public LinkDataPE tryAsLinkData()
    {
        return this;
    }

    @Override
    @Transient
    public boolean isLinkData()
    {
        return true;
    }
}
