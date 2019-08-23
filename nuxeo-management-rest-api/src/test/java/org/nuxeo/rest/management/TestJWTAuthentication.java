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
 *     Thomas Roger
 */

package org.nuxeo.rest.management;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.common.Environment.PRODUCT_NAME;
import static org.nuxeo.ecm.jwt.JWTClaims.CLAIM_SUBJECT;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.jwt.JWTService;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.jaxrs.test.HttpClientTestRule;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.ServletContainerFeature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 11.1
 */
@RunWith(FeaturesRunner.class)
@Features(RestManagementFeature.class)
@Deploy("org.nuxeo.ecm.jwt")
@Deploy("org.nuxeo.rest.management:OSGI-INF/test-jwt-contrib.xml")
@Deploy("org.nuxeo.rest.management:OSGI-INF/test-authentication-contrib.xml")
public class TestJWTAuthentication {

    protected static final String BEARER_SP = "Bearer ";

    @Inject
    protected ServletContainerFeature servletContainerFeature;

    @Inject
    protected JWTService jwtService;

    protected HttpClientTestRule authorizedHttpClientRule;

    protected HttpClientTestRule unauthorizedHttpClientRule;

    protected ObjectMapper mapper = new ObjectMapper();

    @Before
    public void before() {
        String authorizedUserToken = jwtService.newBuilder().withClaim(CLAIM_SUBJECT, "transient/foo").build();
        String unauthorizedUserToken = jwtService.newBuilder().withClaim(CLAIM_SUBJECT, "transient/bar").build();

        String url = String.format("http://localhost:%d/", servletContainerFeature.getPort());
        authorizedHttpClientRule = new HttpClientTestRule.Builder().url(url)
                                                                   .accept(APPLICATION_JSON)
                                                                   .header(HttpHeaders.AUTHORIZATION,
                                                                           BEARER_SP + authorizedUserToken)
                                                                   .build();
        authorizedHttpClientRule.starting();
        unauthorizedHttpClientRule = new HttpClientTestRule.Builder().url(url)
                                                                     .accept(APPLICATION_JSON)
                                                                     .header(HttpHeaders.AUTHORIZATION,
                                                                             BEARER_SP + unauthorizedUserToken)
                                                                     .build();
        unauthorizedHttpClientRule.starting();

        Framework.getProperties().put(ManagementRoot.MANAGEMENT_API_USER_PROPERTY, "transient/foo");
    }

    @After
    public void after() {
        authorizedHttpClientRule.finished();
        unauthorizedHttpClientRule.finished();

        Framework.getProperties().remove(ManagementRoot.MANAGEMENT_API_USER_PROPERTY);
    }

    @Test
    public void testJWTAuthentication() throws IOException {
        try (CloseableClientResponse response = authorizedHttpClientRule.get(TestDistributionObject.PATH)) {
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            JsonNode node = mapper.readTree(response.getEntityInputStream());
            assertTrue(node.has(PRODUCT_NAME));
        }

        try (CloseableClientResponse response = unauthorizedHttpClientRule.get(TestDistributionObject.PATH)) {
            assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        }
    }
}
