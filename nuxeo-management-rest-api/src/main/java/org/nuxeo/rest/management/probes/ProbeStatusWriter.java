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

package org.nuxeo.rest.management.probes;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

import java.io.IOException;
import java.util.Map.Entry;

import org.nuxeo.ecm.core.io.marshallers.json.AbstractJsonWriter;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;
import org.nuxeo.ecm.core.management.api.ProbeStatus;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * see {@link AbstractJsonWriter}
 *
 * @since 11.1
 */
@Setup(mode = SINGLETON, priority = REFERENCE)
public class ProbeStatusWriter extends AbstractJsonWriter<ProbeStatus> {

    @Override
    public void write(ProbeStatus entity, JsonGenerator jg) throws IOException {
        jg.writeStartObject();

        jg.writeBooleanField("neverExecuted", entity.isNeverExecuted());
        jg.writeBooleanField("success", entity.isSuccess());

        jg.writeObjectFieldStart("infos");
        for (Entry<String, String> e : entity.getInfos().entrySet()) {
            jg.writeStringField(e.getKey(), e.getValue());
        }
        jg.writeEndObject();

        jg.writeEndObject();
    }
}
