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
 *     Nour AL KOTOB
 */
package org.nuxeo.rest.management;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.core.bulk.io.BulkConstants.STATUS_COMMAND_ID;

import java.io.IOException;
import java.time.Instant;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

import org.nuxeo.ecm.core.bulk.message.BulkStatus.State;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 11.1
 */
public abstract class ManagementBAFTest extends ManagementBaseTest {

    public static final String MANAGEMENT_BULK_PATH = "site/management/bulk/";

    public static final String STATE = "state";

    @Inject
    protected TransactionalFeature txFeature;

    protected JsonNode runBulkAction(String path, MultivaluedMap<String, String> formData) throws IOException {
        String commandId;
        try (CloseableClientResponse response = httpClientRule.post(path, formData)) {
            JsonNode result = getOkBulkStatusAsJson(response);
            assertEquals(State.SCHEDULED.name(), result.get(STATE).asText());
            commandId = result.get(STATUS_COMMAND_ID).asText();

        }
        return waitAndSeeBAF(commandId);
    }

    protected JsonNode waitAndSeeBAF(String commandId) throws IOException {
        // waiting for the asynchronous BAF task to finish
        txFeature.nextTransaction();

        // checking the bulk action is completed
        try (CloseableClientResponse response = httpClientRule.get(MANAGEMENT_BULK_PATH + commandId)) {
            JsonNode result = getOkBulkStatusAsJson(response);
            assertEquals(State.COMPLETED.name(), result.get(STATE).asText());
            Instant completed = Instant.parse(result.get("completed").asText());
            assertTrue(completed.isBefore(Instant.now()));
            assertNotNull(result.get("processingMillis"));
            return result;
        }
    }

    protected JsonNode getOkBulkStatusAsJson(CloseableClientResponse response) throws IOException {
        assertEquals(SC_OK, response.getStatus());
        return mapper.readTree(response.getEntityInputStream());
    }

}
