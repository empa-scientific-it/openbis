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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Contains information about an attachment. Does not contain attachment content.<br>
 * NOTE: this class does not add anything to the superclass. It exists only because superclasses
 * cannot be mapped to tables in Hibernate.
 * 
 * @author Tomasz Pylak
 */
@Entity
@Table(name = TableNames.EXPERIMENT_ATTACHMENTS_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.EXPERIMENT_COLUMN, ColumnNames.FILE_NAME_COLUMN, ColumnNames.VERSION_COLUMN }) })
public class AttachmentPE extends AbstractAttachmentPE
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final List<AttachmentPE> EMPTY_LIST = Collections.emptyList();

    public static final Set<AttachmentPE> EMPTY_SET = Collections.emptySet();

    public static final AttachmentPE[] EMPTY_ARRAY = new AttachmentPE[0];

}