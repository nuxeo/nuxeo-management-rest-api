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
 *     Nour Al Kotob
 */

package org.nuxeo.rest.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.nuxeo.common.Environment;
import org.nuxeo.ecm.core.io.marshallers.json.JsonAssert;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.rest.management.distribution.SimplifiedServerInfoWriter;
import org.nuxeo.runtime.api.Framework;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 11.1
 */
public class TestDistributionObject extends ManagementBaseTest {

    public static final String PATH = "site/management/distribution";

    @Test
    public void testDistribution() throws IOException {
        final String PRODUCT_NAME = "cool product name";
        final String PRODUCT_VERSION = "cool product version";
        final String DISTRIBUTION_NAME = "cool distribution name";
        final String DISTRIBUTION_VERSION = "cool distribution version";
        final String DISTRIBUTION_SERVER = "cool distribution server";
        final String DISTRIBUTION_DATE = "cool distribution date";

        Map<String, String> testProps = new HashMap<>();
        testProps.put(Environment.PRODUCT_NAME, PRODUCT_NAME);
        testProps.put(Environment.PRODUCT_VERSION, PRODUCT_VERSION);
        testProps.put(Environment.DISTRIBUTION_NAME, DISTRIBUTION_NAME);
        testProps.put(Environment.DISTRIBUTION_VERSION, DISTRIBUTION_VERSION);
        testProps.put(Environment.DISTRIBUTION_SERVER, DISTRIBUTION_SERVER);
        testProps.put(Environment.DISTRIBUTION_DATE, DISTRIBUTION_DATE);
        Framework.getProperties().putAll(testProps);

        try (CloseableClientResponse response = httpClientRule.get(PATH)) {
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            JsonNode node = mapper.readTree(response.getEntityInputStream());
            JsonNode value = node.get(Environment.PRODUCT_NAME);
            assertEquals(PRODUCT_NAME, value.asText());
            value = node.get(Environment.PRODUCT_VERSION);
            assertEquals(PRODUCT_VERSION, value.asText());
            value = node.get(Environment.DISTRIBUTION_NAME);
            assertEquals(DISTRIBUTION_NAME, value.asText());
            value = node.get(Environment.DISTRIBUTION_VERSION);
            assertEquals(DISTRIBUTION_VERSION, value.asText());
            value = node.get(Environment.DISTRIBUTION_SERVER);
            assertEquals(DISTRIBUTION_SERVER, value.asText());
            value = node.get(Environment.DISTRIBUTION_DATE);
            assertEquals(DISTRIBUTION_DATE, value.asText());
            value = node.get(SimplifiedServerInfoWriter.WARNINGS);
            assertNotNull(value);
            value = node.get(Environment.BUNDLES);
            assertNotNull(value);
            String flat = value.toString();
            JsonAssert jAssert = JsonAssert.on(flat).get(0);
            jAssert.get(SimplifiedServerInfoWriter.NAME).notNull();
            jAssert.get(SimplifiedServerInfoWriter.VERSION).notNull();
        } finally {
            testProps.keySet().stream().forEach(key -> Framework.getProperties().remove(key));
        }
    }
}
