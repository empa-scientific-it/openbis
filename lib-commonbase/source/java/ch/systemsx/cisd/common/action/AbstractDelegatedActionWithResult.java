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

package ch.systemsx.cisd.common.action;

/**
 * An abstract superclass for implementations of IDelegatedActionWithResult
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AbstractDelegatedActionWithResult<T> implements IDelegatedActionWithResult<T>
{

    private final T defaultValue;

    public AbstractDelegatedActionWithResult(T defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /**
     * The default implementation of execute -- return the default value.
     */
    public T execute()
    {
        return defaultValue;
    }

    /**
     * The default implementation is to defer to {@link #execute()}.
     */
    @Override
    public T execute(boolean didOperationSucceed)
    {
        return execute();
    }

}
