/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

import java.io.Serializable;

/**
 * A class that holds the information about the boolean result of an operation. There is a way to find out if an error occurred during operation
 * execution, the result is unavailable then.
 * 
 * @author Tomasz Pylak
 */
public class BooleanStatus implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final ResultStatus<Boolean> result;

    private BooleanStatus(Boolean result, boolean errorOccurred, String messageOrNull)
    {
        this.result = new ResultStatus<Boolean>(result, errorOccurred, messageOrNull);
    }

    public static final BooleanStatus createTrue()
    {
        return new BooleanStatus(true, false, null);
    }

    public static final BooleanStatus createFalse(String message)
    {
        assert message != null;
        return new BooleanStatus(false, false, message);
    }

    public static final BooleanStatus createFalse()
    {
        return new BooleanStatus(false, false, null);
    }

    public static final BooleanStatus createError(String message)
    {
        assert message != null;
        return new BooleanStatus(null, true, message);
    }

    /** Use it when error cannot occur during the operation */
    public static BooleanStatus createFromBoolean(boolean result)
    {
        return new BooleanStatus(result, false, null);
    }

    /** @return true if no error occurred and the result was true */
    public boolean isSuccess()
    {
        return isError() == false && getResult() == true;
    }

    /** has operation finished with an error? */
    public boolean isError()
    {
        return result.isError();
    }

    /**
     * You have to be sure that no error occurred before you call this method!
     * 
     * @return result of an operation
     */
    public boolean getResult()
    {
        return result.getResult();
    }

    /** @return the message associated with the result or an error message */
    public String tryGetMessage()
    {
        return result.tryGetMessage();
    }
}
