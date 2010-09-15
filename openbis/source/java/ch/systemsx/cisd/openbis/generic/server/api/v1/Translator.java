/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;

/**
 * @author Franz-Josef Elmer
 */
class Translator
{
    static Role translate(ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy role)
    {
        return translate(role.getRoleCode(), role.getRoleLevel().equals(RoleLevel.SPACE));
    }

    static Role translate(RoleCode roleCode, boolean spaceLevel)
    {
        return new Role(roleCode.name(), spaceLevel);
    }

    static Sample translate(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample privateSample)
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(privateSample.getId());
        initializer.setCode(privateSample.getCode());
        initializer.setIdentifier(privateSample.getIdentifier());
        initializer.setSampleTypeId(privateSample.getSampleType().getId());
        initializer.setSampleTypeCode(privateSample.getSampleType().getCode());
        return new Sample(initializer);
    }

    private Translator()
    {
    }

    public static DataSet translate(ExternalData externalDatum)
    {
        DataSetInitializer initializer = new DataSetInitializer();
        initializer.setCode(externalDatum.getCode());
        return new DataSet(initializer);
    }
}
