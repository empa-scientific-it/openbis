/*
 * Copyright 2007 ETH Zuerich, CISD
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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Persistence Entity representing the type of file format.
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.FILE_FORMAT_TYPES_TABLE, uniqueConstraints =
{ @UniqueConstraint(columnNames =
{ ColumnNames.CODE_COLUMN }) })
public final class FileFormatTypePE extends AbstractTypePE
{
    private static final long serialVersionUID = IServer.VERSION;

    @Override
    @SequenceGenerator(name = SequenceNames.FILE_FORMAT_TYPE_SEQUENCE, sequenceName = SequenceNames.FILE_FORMAT_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.FILE_FORMAT_TYPE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public FileFormatTypePE()
    {
    }

}
