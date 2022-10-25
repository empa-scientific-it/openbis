/*
 * Copyright 2022 ETH Zürich, SIS
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

package ch.ethz.sis.afs.exception;

import ch.ethz.sis.afs.api.dto.ExceptionReason;
import ch.ethz.sis.afs.api.dto.ExceptionType;
import ch.ethz.sis.shared.exception.RuntimeExceptionTemplate;

import java.util.List;

public class FileSystemRuntimeExceptionTemplate extends RuntimeExceptionTemplate<ExceptionReason, ExceptionType> {

    public FileSystemRuntimeExceptionTemplate(int componentCode, Class clazz, List<ExceptionType> serverExceptionTypes, int exceptionCode, String messageTemplate) {
        super(componentCode, clazz, serverExceptionTypes, exceptionCode, messageTemplate);
    }

    @Override
    public ExceptionReason getReason(Object... args) {
        return new ExceptionReason(componentCode, exceptionCode, clazz.getName(), types, String.format(messageTemplate, args));
    }
}
