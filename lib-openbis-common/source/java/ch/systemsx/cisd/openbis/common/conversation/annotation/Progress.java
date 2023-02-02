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

package ch.systemsx.cisd.openbis.common.conversation.annotation;

/**
 * Type of the progress reporting that is used for service conversation methods.
 * 
 * @author pkupczyk
 */
public enum Progress
{

    /**
     * Progress has to be reported by calling
     * {@link ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener#update
     * IServiceConversationProgressListener.update} method. This option should be used when we know how much progress has been made, e.g. we processed
     * 12 out of 100 data sets. If the method execution hangs the progress won't be reported and the conversation will time out.
     */
    MANUAL,

    /**
     * A separate thread regularly sends progress information without a need of calling
     * {@link ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener#update
     * IServiceConversationProgressListener.update} method. This option is useful for methods where we don't have any information about the current
     * progress (e.g. user defined Python script execution) but we still want to notify the client that the processing is in progress and the
     * conversation should not time out. Be aware that the progress will be sent forever if the method execution hangs.
     */
    AUTOMATIC

}
