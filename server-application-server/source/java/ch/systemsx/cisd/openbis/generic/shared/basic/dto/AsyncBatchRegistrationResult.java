/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pkupczyk
 */
public class AsyncBatchRegistrationResult extends BatchRegistrationResult
{

    private static final long serialVersionUID = 1L;

    // GWT
    @SuppressWarnings("unused")
    private AsyncBatchRegistrationResult()
    {
    }

    public AsyncBatchRegistrationResult(String fileName)
    {
        super(fileName,
                "When the import is complete the confirmation or failure report will be sent by email.");
    }

    public static final List<BatchRegistrationResult> singletonList(String fileName)
    {
        List<BatchRegistrationResult> list = new ArrayList<BatchRegistrationResult>();
        list.add(new AsyncBatchRegistrationResult(fileName));
        return list;
    }

}
