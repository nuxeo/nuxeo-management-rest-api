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

package org.nuxeo.rest.management.elasticsearch;

import static org.nuxeo.ecm.core.api.security.SecurityConstants.SYSTEM_USERNAME;
import static org.nuxeo.elasticsearch.bulk.IndexAction.ACTION_NAME;
import static org.nuxeo.elasticsearch.bulk.IndexAction.INDEX_UPDATE_ALIAS_PARAM;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.ecm.core.query.sql.NXQL;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

/**
 * Endpoint to manage Elasticsearch.
 *
 * @since 11.1
 */
@WebObject(type = "elasticsearch")
public class ElasticsearchObject extends DefaultObject {

    public static final String GET_ALL_DOCUMENTS_QUERY = "SELECT * from Document";

    /**
     * Performs an ES indexing on documents matching the optional NXQL query.
     *
     * @param repositoryName the repository name on which we launch the indexing, cannot be {@code null} or
     *            {@code empty}
     * @see #performIndexing(String, String)
     */
    @POST
    @Path("{repositoryName}/reindex")
    public BulkStatus doIndexing(@PathParam("repositoryName") String repositoryName,
            @QueryParam("query") String query) {
        return performIndexing(repositoryName, query);
    }

    /**
     * Performs an ES indexing on the given document and his children.
     *
     * @param repositoryName the repository name that contains the document which will be indexed, cannot be {@code null}
     *            or {@code empty}
     * @param documentId the id of the document that will be indexed and his children recursively
     * @see #performIndexing(String, String)
     */
    @POST
    @Path("{repositoryName}/{documentId}/reindex")
    public BulkStatus doIndexingOnDocument(@PathParam("repositoryName") String repositoryName,
            @PathParam("documentId") String documentId) {
        String query = String.format("Select * From Document where %s = '%s' or %s = '%s'", //
                NXQL.ECM_UUID, documentId, //
                NXQL.ECM_ANCESTORID, documentId);

        return performIndexing(repositoryName, query);
    }

    /**
     * Performs an ES indexing on documents matching the optional NXQL query.
     *
     * @param repositoryName the repository name on which we launch the indexing, cannot be {@code null} or
     *            {@code empty}
     * @param query the NXQL query that documents must match to be indexed, can be {@code null} or {@code empty}, in
     *            this case all documents of the given repository will be indexed {@link #GET_ALL_DOCUMENTS_QUERY}
     * @return the {@link BulkStatus} of the ES indexing
     */
    protected BulkStatus performIndexing(String repositoryName, String query) {
        String nxql = StringUtils.defaultIfBlank(query, GET_ALL_DOCUMENTS_QUERY);
        BulkService bulkService = Framework.getService(BulkService.class);
        String commandId = bulkService.submit(
                new BulkCommand.Builder(ACTION_NAME, nxql).repository(repositoryName)
                                                          .param(INDEX_UPDATE_ALIAS_PARAM, true)
                                                          .user(SYSTEM_USERNAME)
                                                          .build());

        return bulkService.getStatus(commandId);
    }
}
