package ch.systemsx.cisd.etlserver.registrator;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.test.AssertionUtil;

public class TestingDataSetHandlerExpectations
{
    /*
     * shouldas
     */
    protected final boolean shouldRegistrationFail;

    protected final boolean shouldReThrowRollbackException;

    /*
     * happends
     */

    protected boolean didServiceRollbackHappen;

    protected boolean didTransactionRollbackHappen;

    public boolean isShouldRegistrationFail()
    {
        return shouldRegistrationFail;
    }

    public boolean isShouldReThrowRollbackException()
    {
        return shouldReThrowRollbackException;
    }

    public TestingDataSetHandlerExpectations(boolean shouldRegistrationFail,
            boolean shouldReThrowRollbackException)
    {
        super();
        this.shouldRegistrationFail = shouldRegistrationFail;
        this.shouldReThrowRollbackException = shouldReThrowRollbackException;
    }

    public void handleRollbackException(Throwable throwable)
    {
        if (shouldReThrowRollbackException || AssertionUtil.tryAsErrorCausedByUnexpectedInvocation(throwable) != null)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
        } else
        {
            throwable.printStackTrace();
        }
    }
}
