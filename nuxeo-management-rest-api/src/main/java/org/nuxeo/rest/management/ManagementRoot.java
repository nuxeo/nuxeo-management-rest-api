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

import static java.lang.Boolean.TRUE;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 11.1
 */
@Path("/")
@WebObject(type = "management")
public class ManagementRoot extends ModuleRoot {

    public static final String MANAGEMENT_API_USER_PROPERTY = "org.nuxeo.rest.management.user";

    @Path("{path}")
    public Object route(@PathParam("path") String path) {
        // check if the Management API is enabled on this request
        verifyEnabled();

        // check if the user can access the Management API
        verifyUser();

        return newObject(path);
    }

    protected void verifyEnabled() {
        if (!TRUE.equals(ManagementFilter.API_ENABLED.get())) {
            throw new NuxeoException("Requested path doesn't exist", SC_NOT_FOUND);
        }
    }

    protected void verifyUser() {
        NuxeoPrincipal principal = getContext().getPrincipal();
        String managementUser = Framework.getProperty(MANAGEMENT_API_USER_PROPERTY);
        if (principal == null || !(principal.getName().equals(managementUser) || principal.isAdministrator())) {
            throw new NuxeoException(SC_FORBIDDEN);
        }
    }

}
