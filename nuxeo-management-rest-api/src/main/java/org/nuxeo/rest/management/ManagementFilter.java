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

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.nuxeo.launcher.config.ConfigurationGenerator.PARAM_HTTP_PORT;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 11.1
 */
public class ManagementFilter extends HttpFilter {

    private static final long serialVersionUID = 1L;

    protected static final String HTTP_PORT_PROPERTY = "nuxeo.server.http.managementPort";

    public static final String MANAGEMENT_API_USER_PROPERTY = "org.nuxeo.rest.management.user";

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (!requestIsOnConfiguredPort(req)) {
            res.sendError(SC_NOT_FOUND, "Not Found");
        } else if (!isUserValid(req)) {
            res.sendError(SC_FORBIDDEN, "Forbidden");
        } else {
            chain.doFilter(req, res);
        }
    }

    protected boolean requestIsOnConfiguredPort(ServletRequest request) {
        int port = request.getServerPort();
        String configPort = Framework.getProperty(HTTP_PORT_PROPERTY, Framework.getProperty(PARAM_HTTP_PORT));

        return Integer.parseInt(configPort) == port;
    }

    protected boolean isUserValid(HttpServletRequest request) {
        if (request.getUserPrincipal() instanceof NuxeoPrincipal) {
            NuxeoPrincipal principal = (NuxeoPrincipal) request.getUserPrincipal();
            String managementUser = Framework.getProperty(MANAGEMENT_API_USER_PROPERTY);

            return principal.getName().equals(managementUser) || principal.isAdministrator();
        } else {
            return false;
        }
    }
}
