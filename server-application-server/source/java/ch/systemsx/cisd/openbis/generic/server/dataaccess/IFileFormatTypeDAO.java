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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;

/**
 * Interface to the data access layer for retrieving instances of {@link FileFormatTypePE}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IFileFormatTypeDAO extends IGenericDAO<FileFormatTypePE>
{
    /**
     * Tries to find the {@link FileFormatTypePE} for the specified code.
     */
    public FileFormatTypePE tryToFindFileFormatTypeByCode(String code);

    /**
     * Returns all file format types.
     */
    public List<FileFormatTypePE> listFileFormatTypes();

    /**
     * Creates or updates specified file format type.
     */
    public void createOrUpdate(FileFormatTypePE fileFormatType);

}
