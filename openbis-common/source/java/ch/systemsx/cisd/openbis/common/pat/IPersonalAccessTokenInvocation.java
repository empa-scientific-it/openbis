package ch.systemsx.cisd.openbis.common.pat;

public interface IPersonalAccessTokenInvocation
{

    <T> T proceedWithOriginalArguments();

    <T> T proceedWithNewFirstArgument(Object argument);

}
