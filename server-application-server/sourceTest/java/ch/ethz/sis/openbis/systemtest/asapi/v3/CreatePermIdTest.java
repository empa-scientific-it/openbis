package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class CreatePermIdTest extends AbstractTest
{

    @Test
    public void correctAmountOfUniqueIdsGenerated()
    {
        String session = v3api.login(TEST_USER, PASSWORD);
        List<String> batch1 = v3api.createPermIdStrings(session, 3);
        List<String> batch2 = v3api.createPermIdStrings(session, 5);

        Set<String> both = new HashSet<>();
        both.addAll(batch1);
        both.addAll(batch2);

        assertThat(batch1.size(), is(3));
        assertThat(batch2.size(), is(5));
        assertThat(both.size(), is(8));
    }

    @DataProvider(name = "InvalidAmounts")
    public static Object[][] invalidAmounts()
    {
        return new Object[][] { { Integer.MIN_VALUE }, { -1000 }, { -1 }, { 0 }, { 1000 }, { Integer.MAX_VALUE } };
    }

    @Test(dataProvider = "InvalidAmounts", expectedExceptions = UserFailureException.class)
    public void cannotCreateTooManyOrNonPositive(int amount)
    {
        String session = v3api.login(TEST_USER, PASSWORD);
        v3api.createPermIdStrings(session, amount);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        v3api.createPermIdStrings(sessionToken, 3);

        assertAccessLog("create-perm-id-strings  COUNT('3')");
    }

}
