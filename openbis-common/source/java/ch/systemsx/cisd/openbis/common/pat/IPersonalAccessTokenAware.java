package ch.systemsx.cisd.openbis.common.pat;

public interface IPersonalAccessTokenAware
{

    Object createPersonalAccessTokenInvocationHandler(IPersonalAccessTokenInvocation invocation);

}
