package ch.ethz.sis.afsserver;

import ch.ethz.sis.afsserver.impl.ApiServerAdapterTest;
import ch.ethz.sis.afsserver.impl.ApiServerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ApiServerTest.class,
        ApiServerAdapterTest.class
})

public class TestSuite {

}
