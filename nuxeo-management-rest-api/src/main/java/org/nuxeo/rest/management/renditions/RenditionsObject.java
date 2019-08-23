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
 *     Nour Al Kotob
 */

package org.nuxeo.rest.management.renditions;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.SYSTEM_USERNAME;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.ecm.platform.picture.recompute.RecomputeViewsAction;
import org.nuxeo.ecm.platform.thumbnail.action.RecomputeThumbnailsAction;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 11.1
 */
@WebObject(type = "renditions")
public class RenditionsObject extends DefaultObject {

    public static final String THUMBNAILS_DEFAULT_QUERY = "SELECT * FROM Document WHERE ecm:mixinType = 'Thumbnail' AND thumb:thumbnail/data IS NULL AND ecm:isVersion = 0 AND ecm:isProxy = 0 AND ecm:isTrashed = 0";

    public static final String PICTURES_DEFAULT_QUERY = "SELECT * FROM Document WHERE ecm:mixinType = 'Picture' AND picture:views/*/title IS NULL";

    /**
     * Recomputes picture views for the documents matching the given query or {@link #PICTURES_DEFAULT_QUERY} if not
     * provided.
     *
     * @param query a custom query to specify which pictures should be processed
     * @return the {@link BulkStatus} of the command. It contains the commandId To be used in
     *         {@link #doGetPicturesRecomputeStatus(String)} to get an updated {@link BulkStatus}
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("pictures/recompute")
    public BulkStatus doPostPictures(@FormParam("query") String query) {
        String finalQuery = StringUtils.defaultIfBlank(query, PICTURES_DEFAULT_QUERY);
        BulkService bulkService = Framework.getService(BulkService.class);
        String commandId = bulkService.submit(
                new BulkCommand.Builder(RecomputeViewsAction.ACTION_NAME, finalQuery).user(
                        SYSTEM_USERNAME).param(RecomputeViewsAction.PARAM_XPATH, "file:content").build());
        return bulkService.getStatus(commandId);
    }

    /**
     * Retrieves the BulkStatus for the given picture recomputation command id.
     *
     * @param commandId the command id of the BulkStatus to retrieve, which may or may not be
     *            {@link RecomputeViewsAction}
     * @return the BulkStatus concerned by the commandId
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("pictures/recompute/{commandId}")
    public BulkStatus doGetPicturesRecomputeStatus(@PathParam("commandId") String commandId) {
        return getStatus(commandId);
    }

    /**
     * Recomputes the thumbnail for the documents matching the given query or {@link #THUMBNAILS_DEFAULT_QUERY} if not
     * provided.
     *
     * @param query a custom query to specify which thumbnails should be processesd
     * @return the {@link BulkStatus} of the command. It contains the commandId To be used in
     *         {@link #doGetThumbnailsRecomputeStatus(String)} to get an updated {@link BulkStatus}
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("thumbnails/recompute")
    public BulkStatus doPostThumbnails(@FormParam("query") String query) {
        final String finalQuery = StringUtils.defaultIfBlank(query, THUMBNAILS_DEFAULT_QUERY);
        BulkService bulkService = Framework.getService(BulkService.class);
        String commandId = bulkService.submit(
                new BulkCommand.Builder(RecomputeThumbnailsAction.ACTION_NAME, finalQuery).user(SYSTEM_USERNAME)
                                                                                          .build());
        return bulkService.getStatus(commandId);
    }

    /**
     * Retrieves the BulkStatus for the given thumbnail recomputation command id.
     *
     * @param commandId the commandId of the BulkStatus to retrieve. Which may or may not be
     *            {@link RecomputeThumbnailsAction}
     * @return the BulkStatus concerned by the commandId
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("thumbnails/recompute/{commandId}")
    public BulkStatus doGetThumbnailsRecomputeStatus(@PathParam("commandId") String commandId) {
        return getStatus(commandId);
    }

    /**
     * A generic {@link BulkStatus} getter called under the hood by any facade getter.
     *
     * @param commandId the id of the desired command
     * @return the {@link BulkStatus} associated with the commandId.
     */
    protected BulkStatus getStatus(String commandId) {
        if (commandId == null) {
            throw new NuxeoException("The commandId is required", SC_NOT_FOUND);
        }
        return Framework.getService(BulkService.class).getStatus(commandId);
    }

}
