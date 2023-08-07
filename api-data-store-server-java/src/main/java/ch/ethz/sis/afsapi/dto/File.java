/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.afsapi.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class File {
    private String owner;
    private String path;
    private String name;
    private Boolean directory;
    private Long size; // Size in bytes
    private OffsetDateTime lastModifiedTime;
    private OffsetDateTime creationTime;

    @EqualsAndHashCode.Exclude
    private OffsetDateTime lastAccessTime;
}