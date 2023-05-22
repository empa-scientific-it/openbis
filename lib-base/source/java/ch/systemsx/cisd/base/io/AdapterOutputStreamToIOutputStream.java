/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.base.io;

import java.io.IOException;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * An adapter for {@link java.io.OutputStream} that implements {@link IOutputStream}.
 *
 * @author Bernd Rinn
 */
public class AdapterOutputStreamToIOutputStream implements IOutputStream
{
    
    private final java.io.OutputStream delegate;

    public AdapterOutputStreamToIOutputStream(java.io.OutputStream delegate)
    {
        this.delegate = delegate;
    }

    //
    // IOutputStream
    //
    
    @Override
    public void write(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        try
        {
            delegate.write(b, off, len);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Override
    public void write(byte[] b) throws IOExceptionUnchecked
    {
        try
        {
            delegate.write(b);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Override
    public void write(int b) throws IOExceptionUnchecked
    {
        try
        {
            delegate.write(b);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Override
    public void close() throws IOExceptionUnchecked
    {
        try
        {
            delegate.close();
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Override
    public void flush() throws IOExceptionUnchecked
    {
        try
        {
            delegate.flush();
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Override
    public void synchronize() throws IOExceptionUnchecked
    {
        flush();
    }

    //
    // Object
    //

    @Override
    public boolean equals(Object obj)
    {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return delegate.hashCode();
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }

}
