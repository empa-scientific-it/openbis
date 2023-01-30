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

package ch.systemsx.cisd.openbis.uitest.rmi.eager;

import java.util.Collection;
import java.util.HashSet;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.uitest.type.Material;
import ch.systemsx.cisd.openbis.uitest.type.MaterialType;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;

/**
 * @author anttil
 */
public class MaterialRmi extends Material
{

    private ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material material;

    @SuppressWarnings("unused")
    private final String session;

    @SuppressWarnings("unused")
    private final ICommonServer commonServer;

    public MaterialRmi(ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material material,
            String session, ICommonServer commonServer)
    {
        this.material = material;
        this.session = session;
        this.commonServer = commonServer;
    }

    @Override
    public String getCode()
    {
        return material.getMaterialCode();
    }

    @Override
    public Collection<MetaProject> getMetaProjects()
    {
        Collection<MetaProject> metaProjects = new HashSet<MetaProject>();
        for (Metaproject m : material.getMetaprojects())
        {
            metaProjects.add(new MetaProjectRmi(m));
        }
        return metaProjects;
    }

    @Override
    public MaterialType getType()
    {
        throw new UnsupportedOperationException();
    }
}
