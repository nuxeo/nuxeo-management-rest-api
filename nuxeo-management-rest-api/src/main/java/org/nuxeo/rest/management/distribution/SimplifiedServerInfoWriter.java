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

package org.nuxeo.rest.management.distribution;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

import java.io.IOException;

import org.nuxeo.common.Environment;
import org.nuxeo.ecm.admin.runtime.SimplifiedBundleInfo;
import org.nuxeo.ecm.admin.runtime.SimplifiedServerInfo;
import org.nuxeo.ecm.core.io.marshallers.json.AbstractJsonWriter;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;

import com.fasterxml.jackson.core.JsonGenerator;

@Setup(mode = SINGLETON, priority = REFERENCE)
public class SimplifiedServerInfoWriter extends AbstractJsonWriter<SimplifiedServerInfo> {

    public static final String WARNINGS = "warnings";

    public static final String WARNING = "warning";

    public static final String NAME = "name";

    public static final String VERSION = "version";

    @Override
    public void write(SimplifiedServerInfo entity, JsonGenerator jg) throws IOException {
        jg.writeStartObject();
        jg.writeStringField(Environment.PRODUCT_NAME, entity.getApplicationName());
        jg.writeStringField(Environment.PRODUCT_VERSION, entity.getApplicationVersion());
        jg.writeStringField(Environment.DISTRIBUTION_NAME, entity.getDistributionName());
        jg.writeStringField(Environment.DISTRIBUTION_VERSION, entity.getDistributionVersion());
        jg.writeStringField(Environment.DISTRIBUTION_SERVER, entity.getDistributionHost());
        jg.writeStringField(Environment.DISTRIBUTION_DATE, entity.getDistributionDate());
        jg.writeArrayFieldStart(WARNINGS);
        for (String w : entity.getWarnings()) {
            jg.writeStartObject();
            jg.writeStringField(WARNING, w);
            jg.writeEndObject();
        }
        jg.writeEndArray();
        jg.writeArrayFieldStart(Environment.BUNDLES);
        for (SimplifiedBundleInfo b : entity.getBundleInfos()) {
            jg.writeStartObject();
            jg.writeStringField(NAME, b.getName());
            jg.writeStringField(VERSION, b.getVersion());
            jg.writeEndObject();
        }
        jg.writeEndArray();
        jg.writeEndObject();
    }

}
