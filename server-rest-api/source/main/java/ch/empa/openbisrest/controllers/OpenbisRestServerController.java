/*
 *
 *
 * Copyright 2024 Simone Baffelli (simone.baffelli@empa.ch)
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

package ch.empa.openbisrest.controllers;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
//@EnableWebSecurity
@RequestMapping("/api/v3")
public class OpenbisRestServerController {
    private IApplicationServerApi service;

    private static Logger logger = LoggerFactory.getLogger(OpenbisRestServerController.class);


    @Autowired
    public OpenbisRestServerController(IApplicationServerApi service) {
        this.service = service;
    }


    @RequestMapping(path = "/login", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    String login(@RequestParam String username, @RequestParam String password){
        logger.info("login");
        return service.login(username, password);
    }

    @GetMapping(value="/info")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> hello() {
       return service.getServerPublicInformation();
    }

    @GetMapping(value="/search/samples")
    public List<Sample> searchSamples(@Argument String token, @Argument SampleSearchCriteria query, @Argument SampleFetchOptions fetchOptions) {
       return service.searchSamples(token, query, fetchOptions).getObjects();
    }

//    @GetMapping(value="/search/samples")
//    @ResponseBody
//    @ResponseStatus(HttpStatus.OK)
//    public String searchSamples(@RequestParam String,@RequestParam Search , @RequestParam SampleFetchOptions fetchOptions) {
//       return service.searchSamples(token, query).toString();
//    }



}
