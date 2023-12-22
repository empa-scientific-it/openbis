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

package ch.systemsx.cisd.openbis.generic.shared.util;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * Refactoring of SupportedDatePattern - order of entries has a meaning because it starts with the
 * most restrictive format to the least restrictive
 */
public enum SupportedDateTimePattern
{
    ISO_RENDERED_CANONICAL_DATE_PATTERN("yyyy-MM-dd'T'HH:mm:ssXXX"),
    ISO_CANONICAL_DATE_PATTERN("yyyy-MM-dd'T'HH:mm:ssX"),
    ISO_SECONDS_DATE_PATTERN("yyyy-MM-dd'T'HH:mm:ss"),
    ISO_MINUTES_DATE_PATTERN("yyyy-MM-dd'T'HH:mm"),
    RENDERED_CANONICAL_DATE_PATTERN(BasicConstant.RENDERED_CANONICAL_DATE_FORMAT_PATTERN),
    CANONICAL_DATE_PATTERN(BasicConstant.CANONICAL_DATE_FORMAT_PATTERN),
    SECONDS_DATE_PATTERN("yyyy-MM-dd HH:mm:ss"),
    MINUTES_DATE_PATTERN("yyyy-MM-dd HH:mm"),
    DAYS_DATE_PATTERN("yyyy-MM-dd"),
    US_DATE_TIME_PATTERN("M/d/yy h:mm a"),
    US_DATE_TIME_24_PATTERN("M/d/yy HH:mm"),
    US_DATE_PATTERN("M/d/yy");

    private final String pattern;

    SupportedDateTimePattern(String pattern)
    {
        this.pattern = pattern;
    }

    public String getPattern()
    {
        return pattern;
    }
}
