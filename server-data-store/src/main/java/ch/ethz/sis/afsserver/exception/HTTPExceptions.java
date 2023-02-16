/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.exception;

import ch.ethz.sis.afs.api.dto.ExceptionReason;
import ch.ethz.sis.afs.api.dto.ExceptionType;
import ch.ethz.sis.afs.exception.RuntimeExceptionTemplate;
import ch.ethz.sis.shared.exception.ExceptionTemplateHolder;

import java.util.List;

import static ch.ethz.sis.afs.api.dto.ExceptionType.*;

public enum HTTPExceptions implements ExceptionTemplateHolder {
    // APIServer
    UNKNOWN(                                    RuntimeException.class,                         List.of(UnknownError),                             20001, "Unknown error of type %s, please contact support, this error comes with message: %s"),
    INVALID_PARAMETERS(                IllegalArgumentException.class,                 List.of(ClientDeveloperCodingError),               20002, "Invalid parameters"),
    INVALID_HTTP_METHOD(                IllegalArgumentException.class,                 List.of(ClientDeveloperCodingError),               20003, "Invalid HTTP method");

    private RuntimeExceptionTemplate template;

    HTTPExceptions(Class clazz, List<ExceptionType> types, int code, String messageTemplate) {
        this.template = new RuntimeExceptionTemplate(3, clazz, types, code, messageTemplate);
    }

    public RuntimeException getInstance(Object... args) {
        return template.getInstance(args);
    }

    public Exception getCheckedInstance(Object... args) {
        return template.getCheckedInstance(args);
    }

    public static void throwInstance(HTTPExceptions exception, Object... args) {
        throw exception.getInstance(args);
    }

    public ExceptionReason getCause(Object... args) {
        return template.getReason(args);
    }
}
