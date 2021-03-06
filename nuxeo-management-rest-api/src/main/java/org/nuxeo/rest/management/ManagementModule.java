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

import java.util.HashSet;
import java.util.Set;

import org.nuxeo.ecm.webengine.app.JsonNuxeoExceptionWriter;
import org.nuxeo.ecm.webengine.app.WebEngineExceptionMapper;
import org.nuxeo.ecm.webengine.app.WebEngineModule;
import org.nuxeo.ecm.webengine.jaxrs.coreiodelegate.JsonCoreIODelegate;
import org.nuxeo.ecm.webengine.model.io.BlobWriter;

/**
 * @since 11.1
 */
public class ManagementModule extends WebEngineModule {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> result = new HashSet<>();
        result.add(ManagementRoot.class);
        return result;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> result = new HashSet<>();
        result.add(new JsonCoreIODelegate());
        result.add(new WebEngineExceptionMapper());
        result.add(new BlobWriter());
        result.add(new JsonNuxeoExceptionWriter());
        return result;
    }
}
