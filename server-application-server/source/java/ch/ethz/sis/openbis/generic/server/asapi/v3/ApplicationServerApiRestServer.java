package ch.ethz.sis.openbis.generic.server.asapi.v3;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.authentication.configuration.TokenConfig;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@EnableWebSecurity
public class ApplicationServerApiRestServer {
    @Resource(name = ApplicationServerApi.INTERNAL_SERVICE_NAME)
    private IApplicationServerApi service;


    //Here come all the routes
    @RequestMapping(path = "/search/", method = RequestMethod.GET)
    SearchResult<Sample> searchSamples(@RequestHeader(name= TokenConfig.TOKEN_HEADER) String token, SampleSearchCriteria criteria, SampleFetchOptions fetchOptions){
        return service.searchSamples(token, criteria, fetchOptions );
    }


}
