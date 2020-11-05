/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
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
 *     Nour AL KOTOB
 */
package org.nuxeo.rest.management;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.junit.Test;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.runtime.test.runner.Deploy;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 1.0.1
 */
@Deploy("org.nuxeo.rest.management.test:OSGI-INF/dummy-migration-steps.xml")
public class TestMigrationObject extends ManagementBaseTest {

    protected static final String DUMMY_MIGRATION = "dummy-migration";

    protected static final String PATH = "site/management/migration/";

    protected static final String DUMMY_MIGRATION_PATH = PATH + DUMMY_MIGRATION;

    protected static final String DUMMY_MULTI_MIGRATION = "dummy-multi-migration";

    protected static final String DUMMY_MULTI_MIGRATION_PATH = PATH + DUMMY_MULTI_MIGRATION;

    @Test
    public void testGet() throws IOException, JSONException {
        try (CloseableClientResponse response = httpClientRule.get(PATH + DUMMY_MIGRATION)) {
            assertEquals(SC_OK, response.getStatus());
            String json = response.getEntity(String.class);
            assertJsonResponse(json, "json/testGet.json");
        }
    }

    @Test
    public void testGetList() throws IOException, JSONException {
        try (CloseableClientResponse response = httpClientRule.get(PATH)) {
            assertEquals(SC_OK, response.getStatus());
            JsonNode node = mapper.readTree(response.getEntityInputStream());
            Iterator<JsonNode> elements = node.get("entries").elements();
            Map<String, String> entries = new HashMap<>();
            elements.forEachRemaining(n -> entries.put(n.get("id").textValue() , n.toString()));
            assertJsonResponse(entries.get("dummy-migration"), "json/testGet.json");
            assertJsonResponse(entries.get("dummy-multi-migration"), "json/testGetMulti.json");
        }
    }

    @Test
    public void testProbeMigration() throws IOException, JSONException {
        try (CloseableClientResponse response = httpClientRule.post(DUMMY_MIGRATION_PATH + "/probe", null)) {
            assertEquals(SC_OK, response.getStatus());
            String json = response.getEntity(String.class);
            assertJsonResponse(json, "json/testGet.json");
        }
    }

    @Test
    public void testRunMigration() throws IOException, JSONException {
        // Run a unique available migration step
        try (CloseableClientResponse response = httpClientRule.post(DUMMY_MIGRATION_PATH + "/run", null)) {
            assertEquals(SC_ACCEPTED, response.getStatus());
        }
        try (CloseableClientResponse response = httpClientRule.get(PATH + DUMMY_MIGRATION)) {
            assertEquals(SC_OK, response.getStatus());
            String json = response.getEntity(String.class);
            assertJsonResponse(json, "json/testGetAgain.json");
        }
        // Now another migration step is the only one available
        try (CloseableClientResponse response = httpClientRule.post(DUMMY_MIGRATION_PATH + "/run", null)) {
            assertEquals(SC_ACCEPTED, response.getStatus());
        }
        try (CloseableClientResponse response = httpClientRule.get(PATH + DUMMY_MIGRATION)) {
            assertEquals(SC_OK, response.getStatus());
            String json = response.getEntity(String.class);
            assertJsonResponse(json, "json/testGetFinalStep.json");
        }
    }

    @Test
    public void testRunMigrationStep() throws IOException, JSONException {
        // Can't run without specifying the desired step as there are multiple available steps
        try (CloseableClientResponse response = httpClientRule.post(DUMMY_MULTI_MIGRATION_PATH + "/run", null)) {
            assertEquals(SC_BAD_REQUEST, response.getStatus());
        }
        // Run a specific migration step
        try (CloseableClientResponse response = httpClientRule.post(
                PATH + DUMMY_MULTI_MIGRATION + "/run/before-to-reallyAfter", null)) {
            assertEquals(SC_ACCEPTED, response.getStatus());
        }
        try (CloseableClientResponse response = httpClientRule.get(DUMMY_MULTI_MIGRATION_PATH)) {
            assertEquals(SC_OK, response.getStatus());
            String json = response.getEntity(String.class);
            assertJsonResponse(json, "json/testGetFinalStepMulti.json");
        }
    }

    protected void assertJsonResponse(String actual, String expectedFile) throws IOException, JSONException {
        File file = FileUtils.getResourceFileFromContext(expectedFile);
        String expected = readFileToString(file, UTF_8);
        JSONAssert.assertEquals(expected, actual.toString(), true);
    }

}
