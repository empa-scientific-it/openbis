package ch.systemsx.cisd.openbis.generic.shared.pat;

public interface IPersonalAccessTokenConverter
{

    boolean shouldConvert(String sessionTokenOrPAT);

    String convert(String sessionTokenOrPAT);

}
