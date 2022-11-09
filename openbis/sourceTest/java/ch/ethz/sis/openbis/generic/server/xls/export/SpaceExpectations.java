package ch.ethz.sis.openbis.generic.server.xls.export;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class SpaceExpectations extends Expectations
{

    public SpaceExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getSpaces(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        Arrays.asList(new SpacePermId("ELN_SETTINGS"), new SpacePermId("MATERIALS"),
                                new SpacePermId("PUBLICATIONS"))
                )),
                with(any(SpaceFetchOptions.class)));

        will(new CustomAction("getting spaces")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final SpaceFetchOptions fetchOptions = (SpaceFetchOptions) invocation.getParameter(2);

                final Space[] spaces = new Space[3];

                spaces[0] = new Space();
                spaces[0].setFetchOptions(fetchOptions);
                spaces[0].setPermId(new SpacePermId("ELN_SETTINGS"));
                spaces[0].setCode("ELN_SETTINGS");
                spaces[0].setDescription("ELN Settings");

                spaces[1] = new Space();
                spaces[1].setFetchOptions(fetchOptions);
                spaces[1].setPermId(new SpacePermId("MATERIALS"));
                spaces[1].setCode("MATERIALS");
                spaces[1].setDescription("Folder for th materials");

                spaces[2] = new Space();
                spaces[2].setFetchOptions(fetchOptions);
                spaces[2].setPermId(new SpacePermId("PUBLICATIONS"));
                spaces[2].setCode("PUBLICATIONS");
                spaces[2].setDescription("Folder for publications");

                return Arrays.stream(spaces).collect(Collectors.toMap(Space::getPermId, Function.identity(),
                        (space1, space2) -> space2, LinkedHashMap::new));
            }

        });
    }

}
