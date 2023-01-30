from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria

def process(context, parameters):
    name = parameters.get("name");
    searchCriteria = SpaceSearchCriteria();
    searchCriteria.withCode().thatEquals("CISD");
    result = context.applicationService.searchSpaces(context.sessionToken, searchCriteria, SpaceFetchOptions());
    return "hello %s. Spaces: %s" % (name, result.objects)
