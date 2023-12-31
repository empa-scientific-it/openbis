/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
 * @author pkupczyk
 */
public class DownloadException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    private Boolean retriable;

    public DownloadException(String message, Boolean retriable)
    {
        super(message);
        this.retriable = retriable;
    }

    public DownloadException(String message, Throwable throwable, Boolean retriable)
    {
        super(message, throwable);
        this.retriable = retriable;
    }

    public Boolean isRetriable()
    {
        return retriable;
    }

}
