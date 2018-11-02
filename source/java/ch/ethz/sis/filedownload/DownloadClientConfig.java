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

package ch.ethz.sis.filedownload;

/**
 * @author pkupczyk
 */
public class DownloadClientConfig
{

    private ILogger logger;

    private IDownloadServer server;

    private IDownloadStore store;

    private IDeserializerProvider deserializerProvider;

    private IRetryProvider retryProvider;

    public void setLogger(ILogger logger)
    {
        this.logger = logger;
    }

    public ILogger getLogger()
    {
        return logger;
    }

    public void setServer(IDownloadServer server)
    {
        this.server = server;
    }

    public IDownloadServer getServer()
    {
        return server;
    }

    public void setStore(IDownloadStore store)
    {
        this.store = store;
    }

    public IDownloadStore getStore()
    {
        return store;
    }

    public void setDeserializerProvider(IDeserializerProvider deserializerProvider)
    {
        this.deserializerProvider = deserializerProvider;
    }

    public IDeserializerProvider getDeserializerProvider()
    {
        return deserializerProvider;
    }

    public void setRetryProvider(IRetryProvider retryProvider)
    {
        this.retryProvider = retryProvider;
    }

    public IRetryProvider getRetryProvider()
    {
        return retryProvider;
    }

}
