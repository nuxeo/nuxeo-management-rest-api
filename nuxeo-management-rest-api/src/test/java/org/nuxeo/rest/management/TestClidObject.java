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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier.InvalidCLID;
import org.nuxeo.jaxrs.test.CloseableClientResponse;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 11.1
 */
public class TestClidObject extends ManagementBaseTest {

    public static final String PATH = "site/management/clid";

    @Test
    public void testClid() throws IOException, InvalidCLID {
        String expected = "choco--stick";
        new LogicalInstanceIdentifier(expected).save();
        try (CloseableClientResponse response = httpClientRule.get(PATH)) {
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            JsonNode node = mapper.readTree(response.getEntityInputStream());
            JsonNode value = node.get("clid");
            assertEquals(expected, value.asText());
        }
    }

    @Test
    public void testNoClid() {
        LogicalInstanceIdentifier.cleanUp();
        try (CloseableClientResponse response = httpClientRule.get(PATH)) {
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
        }
    }
}
