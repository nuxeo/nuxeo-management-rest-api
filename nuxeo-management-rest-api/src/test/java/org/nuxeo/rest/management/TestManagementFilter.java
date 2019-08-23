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
 *     Kevin Leturc <kleturc@nuxeo.com>
 */
package org.nuxeo.rest.management;

import static org.junit.Assert.assertEquals;
import static org.nuxeo.rest.management.ManagementFilter.HTTP_PORT_PROPERTY;

import java.io.Closeable;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 11.1
 */
public class TestManagementFilter extends ManagementBaseTest {

    public static final String PATH = "site/management/distribution";

    @Test
    public void testDefaultTestConfiguration() {
        try (CloseableClientResponse response = httpClientRule.get(PATH)) {
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        }
    }

    @Test
    public void testWithADifferentConfiguredPort() throws Exception {
        try (Closeable ignored = replaceHttpPort(10); CloseableClientResponse response = httpClientRule.get(PATH)) {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        }
    }

    public Closeable replaceHttpPort(int value) {
        Framework.getProperties().put(HTTP_PORT_PROPERTY, String.valueOf(value));
        return () -> Framework.getProperties().remove(HTTP_PORT_PROPERTY);

    }

}
