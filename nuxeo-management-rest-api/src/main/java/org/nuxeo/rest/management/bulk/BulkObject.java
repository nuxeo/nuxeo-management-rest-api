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
package org.nuxeo.rest.management.bulk;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.ecm.core.bulk.message.BulkStatus.State;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 11.1
 */
@WebObject(type = "bulk")
public class BulkObject extends DefaultObject {

    /**
     * Gets the {@link BulkStatus} for the given {@code commandId}.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{commandId}")
    public BulkStatus doGetStatus(@PathParam("commandId") String commandId) {
        BulkStatus status = Framework.getService(BulkService.class).getStatus(commandId);
        if (status.getState() == State.UNKNOWN) {
            // the command id doesn't exist
            throw new NuxeoException("commandId doesn't exist: " + commandId, SC_NOT_FOUND);
        }
        return status;
    }
}
