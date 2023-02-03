/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.filetransfer;

/**
 * Download server configuration. Can be used to customize a download server behavior.
 * 
 * @author pkupczyk
 */
public class DownloadServerConfig
{

    private ILogger logger;

    private IUserSessionManager sessionManager;

    private IChunkProvider chunkProvider;

    private ISerializerProvider serializerProvider;

    private IConcurrencyProvider concurrencyProvider;

    public void setLogger(ILogger logger)
    {
        this.logger = logger;
    }

    public ILogger getLogger()
    {
        return logger;
    }

    public void setSessionManager(IUserSessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    public IUserSessionManager getSessionManager()
    {
        return sessionManager;
    }

    public void setChunkProvider(IChunkProvider chunkProvider)
    {
        this.chunkProvider = chunkProvider;
    }

    public IChunkProvider getChunkProvider()
    {
        return chunkProvider;
    }

    public void setSerializerProvider(ISerializerProvider serializerProvider)
    {
        this.serializerProvider = serializerProvider;
    }

    public ISerializerProvider getSerializerProvider()
    {
        return serializerProvider;
    }

    public void setConcurrencyProvider(IConcurrencyProvider concurrencyProvider)
    {
        this.concurrencyProvider = concurrencyProvider;
    }

    public IConcurrencyProvider getConcurrencyProvider()
    {
        return concurrencyProvider;
    }

}
