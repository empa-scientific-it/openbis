/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.io.IOException;
import java.io.Writer;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
public abstract class AbstractASyncAction implements IASyncAction
{

    @Override
    public boolean doAction(Writer messageWriter)
    {
        try
        {
            doActionOrThrowException(messageWriter);
        } catch (RuntimeException ex)
        {
            try
            {
                messageWriter.write(getName() + " has failed with a following exception: ");
                messageWriter.write(ex.getMessage());
                messageWriter.write("\n\nPlease correct the error or contact your administrator.");
            } catch (IOException writingEx)
            {
                throw new UserFailureException(writingEx.getMessage()
                        + " when trying to throw exception: " + ex.getMessage(), ex);
            }
            throw ex;
        }
        return true;

    }

    protected abstract void doActionOrThrowException(Writer messageWriter);

}
