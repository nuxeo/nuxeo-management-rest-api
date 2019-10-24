/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/) and others.
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
 *
 * Contributors:
 *     Nour Al Kotob
 */

package org.nuxeo.rest.management;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.nuxeo.jaxrs.test.HttpClientTestRule;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.ServletContainerFeature;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 11.1
 */
@RunWith(FeaturesRunner.class)
@Features(RestManagementFeature.class)
public abstract class ManagementBaseTest {

    public static final String ADMINISTRATOR = "Administrator";

    @Inject
    protected ServletContainerFeature servletContainerFeature;

    protected ObjectMapper mapper = new ObjectMapper();

    protected HttpClientTestRule httpClientRule;

    protected HttpClientTestRule getRule() {
        String url = String.format("http://localhost:%d/", servletContainerFeature.getPort());
        return new HttpClientTestRule.Builder().url(url)
                                               .accept(APPLICATION_JSON)
                                               .credentials(ADMINISTRATOR, ADMINISTRATOR)
                                               .build();
    }

    @Before
    public void before() {
        httpClientRule = getRule();
        httpClientRule.starting();
    }

    @After
    public void after() {
        httpClientRule.finished();
    }
}
