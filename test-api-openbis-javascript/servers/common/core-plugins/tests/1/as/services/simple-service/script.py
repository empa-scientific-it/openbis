from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria

def process(context, parameters):
    spaceCode = parameters.get("space-code")
    searchCriteria = SpaceSearchCriteria()
    searchCriteria.withCode().thatEquals(spaceCode)
    result = context.applicationService.searchSpaces(context.sessionToken, searchCriteria, SpaceFetchOptions())
    return result
