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
 *     Salem Aouana
 */

package org.nuxeo.rest.management.blob.binaries;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.nuxeo.ecm.core.blob.DocumentBlobManager;
import org.nuxeo.ecm.core.blob.binary.BinaryManagerStatus;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

/**
 * Endpoint to manage the binaries.
 *
 * @since 11.1
 */
@WebObject(type = "binaries")
@Produces(APPLICATION_JSON)
public class BinariesObject extends DefaultObject {

    /**
     * Garbage collect the unused (orphaned) binaries.
     * 
     * @return {@link BinaryManagerStatus} if no gc is in progress, otherwise a
     *         {@link javax.ws.rs.core.Response.Status#CONFLICT}
     */
    @DELETE
    @Path("orphaned")
    public Response markAndSweep() {
        DocumentBlobManager documentBlobManager = Framework.getService(DocumentBlobManager.class);

        if (!documentBlobManager.isBinariesGarbageCollectionInProgress()) {
            BinaryManagerStatus binaryManagerStatus = documentBlobManager.garbageCollectBinaries(true);
            return Response.ok(binaryManagerStatus).build();
        }

        return Response.status(CONFLICT).build();
    }
}
