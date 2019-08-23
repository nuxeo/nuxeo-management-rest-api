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

import static java.lang.Boolean.TRUE;
import static org.nuxeo.launcher.config.ConfigurationGenerator.PARAM_HTTP_PORT;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.nuxeo.runtime.api.Framework;

/**
 * @since 11.1
 */
public class ManagementFilter implements Filter {

    protected static final ThreadLocal<Boolean> API_ENABLED = new ThreadLocal<>();

    protected static final String HTTP_PORT_PROPERTY = "nuxeo.server.http.managementPort";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        int port = request.getServerPort();
        String configPort = Framework.getProperty(HTTP_PORT_PROPERTY, Framework.getProperty(PARAM_HTTP_PORT));
        try {
            if (Integer.parseInt(configPort) == port) {
                API_ENABLED.set(TRUE);
            }
            chain.doFilter(request, response);
        } finally {
            API_ENABLED.remove();
        }
    }
}
