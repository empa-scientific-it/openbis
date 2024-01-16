/*
 *
 *
 * Copyright 2023 Simone Baffelli (simone.baffelli@empa.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.rest.controllers;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.ApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.rest.configuration.TokenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import  ch.ethz.sis.openbis.generic.server.asapi.v3.rest.service.PersonalAccessTokenAuthenticationService;

@RestController
//@EnableWebSecurity
@RequestMapping("/api/v3")
public class ApplicationServerApiRestServer {
    @Resource(name = ApplicationServerApi.INTERNAL_SERVICE_NAME)
    private IApplicationServerApi service;

    private static Logger logger = LoggerFactory.getLogger(ApplicationServerApiRestServer.class);



//
//    //Here come all the routes
//    @RequestMapping(path = "/search/", method = RequestMethod.GET)
//    SearchResult<Sample> searchSamples(@RequestHeader(name= TokenConfig.TOKEN_HEADER) String token, SampleSearchCriteria criteria, SampleFetchOptions fetchOptions){
//        return service.searchSamples(token, criteria, fetchOptions );
//    }
//
//    @RequestMapping(path = "/login/", method = RequestMethod.POST)
//    String login(@RequestParam String username, @RequestParam String password){
//        System.out.println("login");
//        return service.login(username, password);
//    }

    @GetMapping(value="/info")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String hello() {
       service.getServerPublicInformation();
        return "Hello World";
    }


}
