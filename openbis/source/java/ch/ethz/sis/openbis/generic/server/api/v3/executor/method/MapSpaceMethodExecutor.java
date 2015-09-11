/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.ISpaceTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
// @Component
public class MapSpaceMethodExecutor extends AbstractMapMethodExecutor<ISpaceId, SpacePE, Space, SpaceFetchOptions> implements IMapSpaceMethodExecutor
{

    @Autowired
    private IMapSpaceByIdExecutor mapExecutor;

    @Autowired
    private ISpaceTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<ISpaceId, SpacePE> getMapExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<SpacePE, Space, SpaceFetchOptions> getTranslator()
    {
        return translator;
    }

}
