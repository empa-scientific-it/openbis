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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.Serializable;

import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;

/**
 * Factory for {@link IImmutableCopier} instances based on {@link FastRecursiveHardLinkMaker}.
 *
 * @author Franz-Josef Elmer
 */
public class ImmutableCopierFactory implements IImmutableCopierFactory, Serializable
{

    private static final long serialVersionUID = 1L;

    @Override
    public IImmutableCopier create(File rsyncExecutable, File lnExecutable)
    {
        return FastRecursiveHardLinkMaker.create(rsyncExecutable, lnExecutable, RSyncConfig.getInstance().getAdditionalCommandLineOptions());
    }

}
