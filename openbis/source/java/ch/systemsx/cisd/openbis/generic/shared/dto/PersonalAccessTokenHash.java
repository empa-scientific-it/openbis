package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.authentication.SessionTokenHash;

public class PersonalAccessTokenHash
{

    // The prefix needs to contain at least one character that is disallowed in a username.
    // Thanks to that we can distinguish a pat token from a regular session token.
    private static final String PREFIX = "$pat-";

    private final String hash;

    private PersonalAccessTokenHash(String hash)
    {
        this.hash = hash;
    }

    public static PersonalAccessTokenHash create(String user, long timestamp)
    {
        return new PersonalAccessTokenHash(PREFIX + SessionTokenHash.create(user, timestamp));
    }

    public static boolean isValid(String hashOrNull)
    {
        if (hashOrNull == null || !hashOrNull.startsWith(PREFIX))
        {
            return false;
        }

        return SessionTokenHash.isValid(hashOrNull.substring(PREFIX.length()));
    }

    @Override public String toString()
    {
        return hash;
    }
}
