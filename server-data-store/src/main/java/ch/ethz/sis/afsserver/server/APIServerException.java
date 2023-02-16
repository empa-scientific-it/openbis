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
package ch.ethz.sis.afsserver.server;

import lombok.NonNull;
import lombok.Value;

@Value
public class APIServerException extends Exception {

    private String id;
    private APIServerErrorType type;
    private Object data;

    public APIServerException(String id, APIServerErrorType type, @NonNull Object data) {
        super(data.toString());
        this.id = id;
        this.type = type;
        this.data = data;
    }

}
