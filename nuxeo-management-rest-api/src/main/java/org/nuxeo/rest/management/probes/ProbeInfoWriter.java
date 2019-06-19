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

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.nuxeo.ecm.core.io.marshallers.json.AbstractJsonWriter;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;
import org.nuxeo.ecm.core.management.api.ProbeInfo;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * see {@link AbstractJsonWriter}
 *
 * @since 11.1
 */
@Setup(mode = SINGLETON, priority = REFERENCE)
public class ProbeInfoWriter extends AbstractJsonWriter<ProbeInfo> {

    @Override
    public void write(ProbeInfo entity, JsonGenerator jg) throws IOException {
        jg.writeStartObject();

        jg.writeStringField("name", entity.getShortcutName());

        writeEntityField("status", entity.getStatus(), jg);

        jg.writeObjectFieldStart("history");
        jg.writeStringField("lastRun", formatDate(entity.getLastRunnedDate()));
        jg.writeStringField("lastSuccess", formatDate(entity.getLastSucceedDate()));
        jg.writeStringField("lastFail", formatDate(entity.getLastFailedDate()));
        jg.writeEndObject();

        jg.writeObjectFieldStart("counts");
        jg.writeNumberField("run", entity.getRunnedCount());
        jg.writeNumberField("success", entity.getSucceedCount());
        jg.writeNumberField("failure", entity.getFailedCount());
        jg.writeEndObject();

        jg.writeNumberField("time", entity.getLastDuration());

        jg.writeEndObject();
    }

    protected String formatDate(Date date) {
        if (date != null) {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
            return ISO_LOCAL_DATE_TIME.format(zdt);
        }
        return null;
    }
}
