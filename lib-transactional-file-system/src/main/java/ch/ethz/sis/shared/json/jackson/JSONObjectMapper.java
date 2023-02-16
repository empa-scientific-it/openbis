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
package ch.ethz.sis.shared.json.jackson;

import java.io.InputStream;
import java.nio.charset.Charset;

public interface JSONObjectMapper {

    Charset getCharset();

    void setCharset(Charset charset);

    <T> T readValue(InputStream src, Class<T> valueType) throws Exception;

    byte[] writeValue(Object value) throws Exception;

}
