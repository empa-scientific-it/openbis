package ch.systemsx.cisd.openbis.generic.shared.pat;

public class PersonalAccessTokenConstants
{

    public static final String PERSONAL_ACCESS_TOKENS_ENABLED_KEY = "personal-access-tokens-enabled";

    public static final boolean PERSONAL_ACCESS_TOKENS_ENABLED_DEFAULT = false;

    public static final String PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD = "personal-access-tokens-max-validity-period";

    public static final long PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD_DEFAULT = 90 * 24 * 60 * 60;

    public static final String PERSONAL_ACCESS_TOKENS_FILE_PATH = "personal-access-tokens-file-path";

    public static final String PERSONAL_ACCESS_TOKENS_FILE_PATH_DEFAULT = "../../../personal-access-tokens.json";

}
