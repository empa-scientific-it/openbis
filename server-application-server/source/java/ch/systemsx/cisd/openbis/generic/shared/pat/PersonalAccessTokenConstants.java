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
package ch.systemsx.cisd.openbis.generic.shared.pat;

public class PersonalAccessTokenConstants
{

    private static final long DAY_IN_SECONDS = 24 * 60 * 60;

    public static final String PERSONAL_ACCESS_TOKENS_ENABLED_KEY = "personal-access-tokens-enabled";

    public static final boolean PERSONAL_ACCESS_TOKENS_ENABLED_DEFAULT = true;

    public static final String PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD = "personal-access-tokens-max-validity-period";

    public static final long PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD_DEFAULT = 30 * DAY_IN_SECONDS;

    public static final String PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD = "personal-access-tokens-validity-warning-period";

    public static final long PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD_DEFAULT = 5 * DAY_IN_SECONDS;

    public static final String PERSONAL_ACCESS_TOKENS_FILE_PATH = "personal-access-tokens-file-path";

    public static final String PERSONAL_ACCESS_TOKENS_FILE_PATH_DEFAULT = "../../../personal-access-tokens.json";

}
