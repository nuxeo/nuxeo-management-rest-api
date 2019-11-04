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

import static org.nuxeo.ecm.core.api.security.SecurityConstants.SYSTEM_USERNAME;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
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
@Produces(MediaType.APPLICATION_JSON)
public class RenditionsObject extends DefaultObject {

    public static final String THUMBNAILS_DEFAULT_QUERY = "SELECT * FROM Document WHERE ecm:mixinType = 'Thumbnail' AND thumb:thumbnail/data IS NULL AND ecm:isVersion = 0 AND ecm:isProxy = 0 AND ecm:isTrashed = 0";

    public static final String PICTURES_DEFAULT_QUERY = "SELECT * FROM Document WHERE ecm:mixinType = 'Picture' AND picture:views/*/title IS NULL";

    /**
     * Recomputes picture views for the documents matching the given query or {@link #PICTURES_DEFAULT_QUERY} if not
     * provided.
     *
     * @param query a custom query to specify which pictures should be processed
     * @return the {@link BulkStatus} of the command
     */
    @POST
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
     * Recomputes the thumbnail for the documents matching the given query or {@link #THUMBNAILS_DEFAULT_QUERY} if not
     * provided.
     *
     * @param query a custom query to specify which thumbnails should be processesd
     * @return the {@link BulkStatus} of the command
     */
    @POST
    @Path("thumbnails/recompute")
    public BulkStatus doPostThumbnails(@FormParam("query") String query) {
        final String finalQuery = StringUtils.defaultIfBlank(query, THUMBNAILS_DEFAULT_QUERY);
        BulkService bulkService = Framework.getService(BulkService.class);
        String commandId = bulkService.submit(
                new BulkCommand.Builder(RecomputeThumbnailsAction.ACTION_NAME, finalQuery).user(SYSTEM_USERNAME)
                                                                                          .build());
        return bulkService.getStatus(commandId);
    }

}
