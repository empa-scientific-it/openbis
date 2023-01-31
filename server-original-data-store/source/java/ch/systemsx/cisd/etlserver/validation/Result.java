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

package ch.systemsx.cisd.etlserver.validation;

/**
 * Result of a column header validation.
 *
 * @author Franz-Josef Elmer
 */
public class Result
{
    public static final Result OK = new Result(null);

    public static final Result failure(String failureMessage)
    {
        assert failureMessage != null;
        return new Result(failureMessage);
    }

    private final String failureMessage;

    private Result(String failureMessage)
    {
        this.failureMessage = failureMessage;
    }

    public boolean isValid()
    {
        return failureMessage == null;
    }

    @Override
    public String toString()
    {
        return failureMessage == null ? "" : failureMessage;
    }

}
