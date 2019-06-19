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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.nuxeo.ecm.core.io.marshallers.json.JsonAssert;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.runtime.test.runner.Deploy;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 11.1
 */
@Deploy("org.nuxeo.ecm.core.management")
public class TestProbesObject extends ManagementBaseTest {

    private static final String FAILURE_COUNT = "failure";

    private static final String SUCCESS_COUNT = "success";

    protected static final String RUN_COUNT = "run";

    protected static final String COUNTS = "counts";

    protected static final String ADMINISTRATIVE_STATUS = "administrativeStatus";

    protected static final String STATUS_INFOS = "infos";

    protected static final String STATUS_SUCCESS = SUCCESS_COUNT;

    protected static final String NAME = "name";

    public static final String PATH = "site/management/probes";

    @Test
    public void testAllProbes() throws IOException {
        try (CloseableClientResponse response = httpClientRule.get(PATH)) {
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            JsonNode node = mapper.readTree(response.getEntityInputStream());
            JsonAssert jAssert = JsonAssert.on(node.toString());
            jAssert.get("entity-type").isEquals("probes");
            JsonAssert jProbeArray = jAssert.get("entries");

            JsonAssert jProbe = jProbeArray.get(0);
            testProbeInfo(jProbe);
        }
    }

    @Test
    public void testProbe() throws IOException {
        try (CloseableClientResponse response = httpClientRule.get(PATH + "/" + ADMINISTRATIVE_STATUS)) {
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            JsonNode node = mapper.readTree(response.getEntityInputStream());
            JsonAssert jAssert = JsonAssert.on(node.toString());
            testProbeInfo(jAssert);
        }
    }

    @Test
    public void testLaunchProbe() throws IOException {
        int runCount;
        try (CloseableClientResponse response = httpClientRule.get(PATH + "/" + ADMINISTRATIVE_STATUS)) {
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            JsonNode node = mapper.readTree(response.getEntityInputStream());
            runCount = node.get(COUNTS).get(RUN_COUNT).asInt();
        }
        try (CloseableClientResponse response = httpClientRule.post(PATH + "/" + ADMINISTRATIVE_STATUS, null)) {
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            JsonNode node = mapper.readTree(response.getEntityInputStream());
            JsonAssert jAssert = JsonAssert.on(node.toString());
            testProbeInfo(jAssert);
            assertEquals(runCount + 1L, node.get(COUNTS).get(RUN_COUNT).asInt());
        }
    }

    protected void testProbeInfo(JsonAssert jProbe) throws IOException {
        jProbe.get(NAME).notNull();
        jProbe.get("status").get(STATUS_SUCCESS).notNull();
        jProbe.get("status").get(STATUS_INFOS).notNull();

        JsonAssert jHistory = jProbe.get("history");
        jHistory.has("lastRun");
        jHistory.has("lastSuccess");
        jHistory.has("lastFail");

        JsonAssert jCounts = jProbe.get(COUNTS);
        jCounts.has(RUN_COUNT);
        jCounts.has(SUCCESS_COUNT);
        jCounts.has(FAILURE_COUNT);
        jProbe.get("time").notNull();
    }
}
