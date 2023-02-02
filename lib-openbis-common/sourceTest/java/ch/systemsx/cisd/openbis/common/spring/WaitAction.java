package ch.systemsx.cisd.openbis.common.spring;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

/**
 * @author Franz-Josef Elmer
 */
public final class WaitAction implements Action
{
    private final long waitingTime;

    public WaitAction(long waitingTime)
    {
        this.waitingTime = waitingTime;
    }

    @Override
    public Object invoke(Invocation inv) throws Throwable
    {
        Thread.sleep(waitingTime);
        return null;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("wait " + waitingTime);
    }
}
