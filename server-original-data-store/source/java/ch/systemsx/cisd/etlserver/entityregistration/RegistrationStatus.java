/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.entityregistration;

/**
 * An interface to describe success and errors encountered during registration.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
interface IRegistrationStatus
{
    boolean isError();

    Throwable getError();

    String getMessage();
}

/**
 * Abstract superclass for success and errors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractRegistrationStatus implements IRegistrationStatus
{
    protected AbstractRegistrationStatus()
    {
    }
}

class RegistrationError extends AbstractRegistrationStatus
{
    private final Throwable error;

    RegistrationError(Throwable error)
    {
        this.error = error;
    }

    @Override
    public boolean isError()
    {
        return true;
    }

    @Override
    public Throwable getError()
    {
        return error;
    }

    @Override
    public String getMessage()
    {
        return error.getMessage();
    }
}

class RegistrationSuccess extends AbstractRegistrationStatus
{
    RegistrationSuccess(String[] registeredMetadata)
    {
    }

    @Override
    public boolean isError()
    {
        return false;
    }

    @Override
    public Throwable getError()
    {
        assert false : "getError() should not be called on a success object";

        return null;
    }

    @Override
    public String getMessage()
    {
        return "Success";
    }
}
