/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.context;

/**
 * @author pkupczyk
 */
public class ProgressFormatter
{

    public static String formatShort(IProgress progress)
    {
        StringBuilder result = new StringBuilder();

        if (progress.getTotalItemsToProcess() == null && progress.getNumItemsProcessed() == null)
        {
            result.append(progress.getLabel());
        } else
        {
            result.append(progress.getLabel() + " (" + progress.getNumItemsProcessed() + "/" + progress.getTotalItemsToProcess() + ")");
        }

        return result.toString();
    }

    public static String format(IProgress progress)
    {
        StringBuilder result = new StringBuilder();

        result.append(formatShort(progress));

        if (progress.getDetails() != null)
        {
            result.append(" " + progress.getDetails());
        }

        return result.toString();
    }

}
