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

package org.nuxeo.rest.management;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.core.bulk.io.BulkConstants.STATUS_ERROR_COUNT;
import static org.nuxeo.ecm.core.bulk.io.BulkConstants.STATUS_ERROR_MESSAGE;
import static org.nuxeo.ecm.core.bulk.io.BulkConstants.STATUS_HAS_ERROR;
import static org.nuxeo.ecm.core.bulk.io.BulkConstants.STATUS_PROCESSED;
import static org.nuxeo.ecm.core.bulk.io.BulkConstants.STATUS_TOTAL;
import static org.nuxeo.ecm.platform.picture.api.adapters.AbstractPictureAdapter.VIEWS_PROPERTY;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailService;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @since 11.1
 */
@Features(AutomationFeature.class)
@Deploy("org.nuxeo.ecm.core.management")
@Deploy("org.nuxeo.ecm.platform.tag")
@Deploy("org.nuxeo.ecm.platform.picture.api")
@Deploy("org.nuxeo.ecm.platform.picture.core")
@Deploy("org.nuxeo.ecm.platform.picture.convert")
@Deploy("org.nuxeo.ecm.platform.convert")
@Deploy("org.nuxeo.ecm.platform.types.api")
@Deploy("org.nuxeo.ecm.platform.types.core")
@Deploy("org.nuxeo.ecm.platform.thumbnail")
public class TestRenditionsObject extends ManagementBAFTest {

    public static final String STATE = "state";

    public static final String ASYNC_KEY = "async";

    public static final String QUERY_KEY = "query";

    public static final String INVALID_QUERY = "Invalid query";

    public static final String WRONG_QUERY = "SELECT * FROM nowhere";

    public static final String PATH = "site/management/renditions";

    public static final String PICTURES_RECOMPUTE_PATH = PATH + "/pictures/recompute";

    public static final String THUMBNAILS_RECOMPUTE_PATH = PATH + "/thumbnails/recompute";

    @Inject
    protected CoreSession session;

    @Inject
    protected ThumbnailService thumbnailService;

    @Test
    public void testGetWrongCommandId() {
        try (CloseableClientResponse response = httpClientRule.get(PATH + "/pictures/" + "fakeCommandId")) {
            assertEquals(SC_NOT_FOUND, response.getStatus());
        }
    }

    @Test
    public void testRenditionsPostPicturesRecompute() throws IOException {
        // test the picture views are computed correctly
        DocumentRef docRef = beforePictureViewsRecompute();

        // empty picture views so the default query selects the documents
        docRef = emptyPictureViews(docRef);

        // generating new picture views
        JsonNode result = runBulkAction(PICTURES_RECOMPUTE_PATH, null);

        // checking the picture views are recomputed in the session and the bulk status
        afterPicturesRecompute(docRef, false);
        assertEquals(1, result.get(STATUS_PROCESSED).asInt());
        assertFalse(result.get(STATUS_HAS_ERROR).asBoolean());
        assertEquals(0, result.get(STATUS_ERROR_COUNT).asInt());
        assertEquals(1, result.get(STATUS_TOTAL).asInt());
    }

    @Test
    public void testRenditionsPostPicturesRecomputeCustomQuery() throws IOException {
        // test the picture views are computed correctly
        DocumentRef docRef = beforePictureViewsRecompute();

        // preparing a custom query
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(QUERY_KEY, "SELECT * FROM Document WHERE ecm:mixinType = 'Picture'");
        formData.add(ASYNC_KEY, "true");

        // generating new picture views
        JsonNode result = runBulkAction(PICTURES_RECOMPUTE_PATH, formData);

        // checking the picture views are recomputed in the session and the bulk status
        afterPicturesRecompute(docRef, false);
        assertEquals(1, result.get(STATUS_PROCESSED).asInt());
        assertFalse(result.get(STATUS_HAS_ERROR).asBoolean());
        assertEquals(0, result.get(STATUS_ERROR_COUNT).asInt());
        assertEquals(1, result.get(STATUS_TOTAL).asInt());
    }

    @Test
    public void testRenditionsPostPicturesRecomputeWrongQuery() throws IOException {
        // test the picture views are computed correctly and emptying them to check if the bulk action will recompute
        // them correctly
        DocumentRef docRef = beforePictureViewsRecompute();
        docRef = emptyPictureViews(docRef);

        // generating new picture views
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(QUERY_KEY, WRONG_QUERY);
        formData.add(ASYNC_KEY, "true");
        JsonNode result = runBulkAction(PICTURES_RECOMPUTE_PATH, formData);

        // checking the picture views are not recomputed because the query failed
        afterPicturesRecompute(docRef, true);
        assertEquals(0, result.get(STATUS_PROCESSED).asInt());
        assertTrue(result.get(STATUS_HAS_ERROR).asBoolean());
        assertEquals(1, result.get(STATUS_ERROR_COUNT).asInt());
        assertEquals(0, result.get(STATUS_TOTAL).asInt());
        assertEquals(INVALID_QUERY, result.get(STATUS_ERROR_MESSAGE).asText());
    }

    @Test
    public void testRenditionsPostThumbnailsRecomputeBulkStatus() throws IOException {
        // test the thumbnails are computed correctly
        DocumentRef docRef = beforeThumbnailsRecompute();

        // empty picture views so the default query selects the documents
        docRef = emptyThumbnail(docRef);

        // generating new thumbnails
        JsonNode result = runBulkAction(THUMBNAILS_RECOMPUTE_PATH, null);

        // checking the thumbnails are recomputed in the session and the bulk status
        afterThumbnailsRecompute(docRef, false);
        assertEquals(1, result.get(STATUS_PROCESSED).asInt());
        assertFalse(result.get(STATUS_HAS_ERROR).asBoolean());
        assertEquals(0, result.get(STATUS_ERROR_COUNT).asInt());
        assertEquals(1, result.get(STATUS_TOTAL).asInt());
    }

    @Test
    public void testRenditionsPostThumbnailsRecomputeBulkStatusCustomQuery() throws IOException {
        // test the thumbnails are computed correctly and emptying them to check if the bulk action will recompute them
        // correctly
        DocumentRef docRef = beforeThumbnailsRecompute();

        // generating new thumbnails
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(QUERY_KEY, "SELECT * FROM Document WHERE ecm:mixinType = 'Thumbnail'");
        formData.add(ASYNC_KEY, "true");
        JsonNode result = runBulkAction(THUMBNAILS_RECOMPUTE_PATH, formData);

        // checking the thumbnails are recomputed in the session and in the bulk status
        afterThumbnailsRecompute(docRef, false);
        assertEquals(1, result.get(STATUS_PROCESSED).asInt());
        assertFalse(result.get(STATUS_HAS_ERROR).asBoolean());
        assertEquals(0, result.get(STATUS_ERROR_COUNT).asInt());
        assertEquals(1, result.get(STATUS_TOTAL).asInt());
    }

    @Test
    @Deploy("org.nuxeo.ecm.platform.thumbnail")
    public void testRenditionsPostThumbnailsRecomputeBulkStatusWrongQuery() throws IOException {
        // test the thumbnails are computed correctly and emptying them to check if the bulk action will recompute them
        // correctly
        DocumentRef docRef = beforeThumbnailsRecompute();
        docRef = emptyThumbnail(docRef);

        // generating new thumbnails
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(QUERY_KEY, WRONG_QUERY);
        formData.add(ASYNC_KEY, "true");
        JsonNode result = runBulkAction(THUMBNAILS_RECOMPUTE_PATH, formData);

        // checking the thumbnails are not recomputed in the session because the query failed
        afterThumbnailsRecompute(docRef, true);
        assertEquals(0, result.get(STATUS_PROCESSED).asInt());
        assertTrue(result.get(STATUS_HAS_ERROR).asBoolean());
        assertEquals(1, result.get(STATUS_ERROR_COUNT).asInt());
        assertEquals(0, result.get(STATUS_TOTAL).asInt());
        assertEquals(INVALID_QUERY, result.get(STATUS_ERROR_MESSAGE).asText());
    }

    @SuppressWarnings("unchecked")
    protected DocumentRef beforePictureViewsRecompute() throws IOException {
        DocumentModel doc = session.createDocumentModel("/", "pictureDoc", "Picture");
        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext("images/test.jpg"), "image/jpeg",
                StandardCharsets.UTF_8.name(), "test.jpg");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);

        // wait for picture views generation
        txFeature.nextTransaction();
        doc = session.getDocument(doc.getRef());
        List<Serializable> pictureViews = (List<Serializable>) doc.getPropertyValue(VIEWS_PROPERTY);
        assertNotNull(pictureViews);
        assertFalse(pictureViews.isEmpty());

        return doc.getRef();
    }

    @SuppressWarnings("unchecked")
    protected void afterPicturesRecompute(DocumentRef docRef, boolean shouldBeEmpty) {
        DocumentModel doc = session.getDocument(docRef);
        List<Serializable> pictureViews = (List<Serializable>) doc.getPropertyValue(VIEWS_PROPERTY);
        assertNotNull(pictureViews);
        assertEquals(shouldBeEmpty, pictureViews.isEmpty());
    }

    protected DocumentRef beforeThumbnailsRecompute() throws IOException {
        DocumentModel doc = session.createDocumentModel("/", "testDoc", "File");
        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext("images/test.jpg"), "image/jpeg",
                StandardCharsets.UTF_8.name(), "test.jpg");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);

        // wait for thumbnail generation
        txFeature.nextTransaction();
        doc = session.getDocument(doc.getRef());
        Blob thumbnail = thumbnailService.getThumbnail(doc, session);
        assertNotNull(thumbnail);

        return doc.getRef();
    }

    protected void afterThumbnailsRecompute(DocumentRef docRef, boolean shouldBeNull) {
        DocumentModel doc = session.getDocument(docRef);
        Blob thumbnail = thumbnailService.getThumbnail(doc, session);
        assertEquals(shouldBeNull, thumbnail == null);
    }

    protected DocumentRef emptyThumbnail(DocumentRef docRef) {
        DocumentModel doc = session.getDocument(docRef);
        doc.setPropertyValue("thumbnail:thumbnail", null);
        session.saveDocument(doc);
        txFeature.nextTransaction();
        doc = session.getDocument(doc.getRef());
        Blob thumbnail = thumbnailService.getThumbnail(doc, session);
        assertNull(thumbnail);
        return doc.getRef();
    }

    @SuppressWarnings("unchecked")
    protected DocumentRef emptyPictureViews(DocumentRef docRef) {
        DocumentModel doc = session.getDocument(docRef);
        doc.setPropertyValue(VIEWS_PROPERTY, new ArrayList<>());
        session.saveDocument(doc);
        txFeature.nextTransaction();
        doc = session.getDocument(doc.getRef());
        List<Serializable> pictureViews = (List<Serializable>) doc.getPropertyValue(VIEWS_PROPERTY);
        assertNotNull(pictureViews);
        assertTrue(pictureViews.isEmpty());
        return doc.getRef();
    }
}
