package ch.systemsx.cisd.authentication;

public class SessionManagerLock
{

    private static final SessionManagerLock instance = new SessionManagerLock();

    private SessionManagerLock(){}

    public static SessionManagerLock getInstance(){
        return instance;
    }
}
