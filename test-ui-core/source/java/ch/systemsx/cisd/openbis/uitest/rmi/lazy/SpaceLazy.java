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

package ch.systemsx.cisd.openbis.uitest.rmi.lazy;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.uitest.type.Space;

/**
 * @author anttil
 */
public class SpaceLazy extends Space
{
    private final String code;

    @SuppressWarnings("unused")
    private final String session;

    @SuppressWarnings("unused")
    private final ICommonServer commonServer;

    public SpaceLazy(String code, String session, ICommonServer commonServer)
    {
        this.code = code;
        this.session = session;
        this.commonServer = commonServer;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public String getDescription()
    {
        throw new UnsupportedOperationException();
    }

}
