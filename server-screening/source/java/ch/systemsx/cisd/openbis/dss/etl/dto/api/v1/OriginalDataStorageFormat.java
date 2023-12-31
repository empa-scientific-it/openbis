/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

/**
 * How original dataset data should be stored.
 * 
 * @author Tomasz Pylak
 */
public enum OriginalDataStorageFormat
{
    /** original data are stored as they come (without additional compression) */
    UNCHANGED,

    /** original data are stored in one HDF5 container (uncompressed) */
    HDF5,

    /** original data are stored in one HDF5 container (compressed) */
    HDF5_COMPRESSED;

    public boolean isHdf5()
    {
        return this == OriginalDataStorageFormat.HDF5
                || this == OriginalDataStorageFormat.HDF5_COMPRESSED;
    }

    public ch.systemsx.cisd.openbis.dss.etl.dto.api.OriginalDataStorageFormat getIndependentOriginalDataStorageFormat()
    {
        switch (this)
        {
            case HDF5:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.OriginalDataStorageFormat.HDF5;
            case HDF5_COMPRESSED:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.OriginalDataStorageFormat.HDF5_COMPRESSED;
            case UNCHANGED:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.OriginalDataStorageFormat.UNCHANGED;
        }
        return null; // impossible
    }
}