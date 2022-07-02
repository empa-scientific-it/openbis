package ch.systemsx.cisd.openbis.generic.server.pat;

public interface IPersonalAccessTokenConverter
{

    boolean shouldConvert(String sessionTokenOrPAT);

    String convert(String sessionTokenOrPAT);

}
