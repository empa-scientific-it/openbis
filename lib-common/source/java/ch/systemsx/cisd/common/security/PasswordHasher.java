/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A class that provides function for computing salted (SHA1) hashes of passwords and comparing plain passwords to those hashes for match or mismatch.
 * 
 * @author Bernd Rinn
 */
public class PasswordHasher
{

    private static final int SALT_LENGTH = 4;

    private static byte[] createSalt()
    {
        final Random rng = new Random();
        final byte[] result = new byte[SALT_LENGTH];
        rng.nextBytes(result);
        return result;
    }

    private static byte[] computeSaltedSHA1Hash(String password, byte[] salt)
    {
        assert password != null : "Unspecified password.";

        try
        {
            final MessageDigest algorithm = MessageDigest.getInstance("SHA1");
            algorithm.update(salt);
            algorithm.update(password.getBytes("utf8"));
            return algorithm.digest();
        } catch (NoSuchAlgorithmException ex)
        {
            throw new Error("SHA1 hashing algorithms not supported.");
        } catch (UnsupportedEncodingException ex)
        {
            throw new Error("UTF8 encoding is not supported.");
        }
    }

    private static String computeHashedPassword(String password, byte[] salt)
    {
        final byte[] encoded = computeSaltedSHA1Hash(password, salt);
        final byte[] encodedWithSalt = ArrayUtils.addAll(salt, encoded);
        return encodeBase64(encodedWithSalt);
    }

    private static byte[] getSaltFromPassword(byte[] saltedHashedPassword)
    {
        return ArrayUtils.subarray(saltedHashedPassword, 0, SALT_LENGTH);
    }

    private static byte[] getHashFromPassword(byte[] saltedHashedPassword)
    {
        return ArrayUtils.subarray(saltedHashedPassword, SALT_LENGTH, saltedHashedPassword.length);
    }

    private static String encodeBase64(byte[] base64Data)
    {
        try
        {
            return new String(Base64.encodeBase64(base64Data), "utf8");
        } catch (UnsupportedEncodingException ex)
        {
            throw new Error("UTF8 encoding is not supported.");
        }
    }

    private static byte[] decodeBase64(String string)
    {
        try
        {
            return Base64.decodeBase64(string.getBytes("utf8"));
        } catch (UnsupportedEncodingException ex)
        {
            throw new Error("UTF8 encoding is not supported.");
        }
    }

    /**
     * Computes a salted hash for the given <var>password</var>.
     */
    public static String computeSaltedHash(String password)
    {
        final byte[] salt = createSalt();
        return computeHashedPassword(password, salt);
    }

    /**
     * Returns <code>true</code> if the given <var>password</var> matches the <var>saltedHash</var> as computed by {@link #computeSaltedHash(String)}.
     */
    public static boolean isPasswordCorrect(String password, String saltedHash)
    {
        final byte[] reference = decodeBase64(saltedHash);
        final byte[] salt = getSaltFromPassword(reference);
        final byte[] hashReference = getHashFromPassword(reference);
        final byte[] hashtoCheck = computeSaltedSHA1Hash(password, salt);
        return Arrays.equals(hashtoCheck, hashReference);
    }

}
