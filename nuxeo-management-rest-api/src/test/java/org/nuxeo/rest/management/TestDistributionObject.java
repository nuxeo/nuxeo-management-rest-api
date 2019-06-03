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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.jaxrs.test.JerseyClientHelper;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.ServletContainerFeature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * @since 11.1
 */
@RunWith(FeaturesRunner.class)
@Features(ManagementFeature.class)
public class TestDistributionObject {

    public static final String DISTRIBUTION_PATH = "site/management/distribution";

    @Inject
    protected ServletContainerFeature servletContainerFeature;

    protected Client client;

    protected ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapper();
        client = JerseyClientHelper.clientBuilder().setCredentials("Administrator", "Administrator").build();
    }

    @Test
    public void testDistribution() throws IOException {
        try (CloseableClientResponse response = get()) {
            assertEquals(200, response.getStatus());
            JsonNode node = mapper.readTree(response.getEntityInputStream());
            JsonNode foo = node.get("foo");
            assertNotNull(foo);
            assertEquals("bar", foo.asText());
        }
    }

    protected String getBaseURL() {
        int port = servletContainerFeature.getPort();
        return "http://localhost:" + port + "/";
    }

    protected CloseableClientResponse get() {
        WebResource wr = client.resource(getBaseURL() + DISTRIBUTION_PATH);
        WebResource.Builder builder = wr.getRequestBuilder();
        return CloseableClientResponse.of(builder.get(ClientResponse.class));
    }
}
