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

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.SYSTEM_USERNAME;

import java.io.IOException;

import org.junit.Test;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.platform.picture.recompute.RecomputeViewsAction;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;

/**
 * @since 11.1
 */
@Deploy("org.nuxeo.ecm.core.test:OSGI-INF/bulk-sequential-contrib.xml")
public class TestBulkObject extends ManagementBAFTest {

    @Test
    public void testGetStatus() throws IOException {
        BulkService bulkService = Framework.getService(BulkService.class);
        String commandId = bulkService.submit(new BulkCommand.Builder("dummySequential", "SELECT * FROM Document").user(
                SYSTEM_USERNAME).param(RecomputeViewsAction.PARAM_XPATH, "file:content").build());
        waitAndSeeBAF(commandId);
    }

    @Test
    public void testGetStatusWithWrongCommandId() {
        try (CloseableClientResponse response = httpClientRule.get(MANAGEMENT_BULK_PATH + "fakeCommandId")) {
            assertEquals(SC_NOT_FOUND, response.getStatus());
        }
    }

}
