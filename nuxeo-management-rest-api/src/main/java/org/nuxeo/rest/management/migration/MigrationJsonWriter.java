/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
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
package org.nuxeo.rest.management.migration;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;
import static org.nuxeo.rest.management.migration.MigrationConstants.MIGRATION_DESCRIPTION;
import static org.nuxeo.rest.management.migration.MigrationConstants.MIGRATION_DESCRIPTION_LABEL;
import static org.nuxeo.rest.management.migration.MigrationConstants.MIGRATION_ENTITY_TYPE;
import static org.nuxeo.rest.management.migration.MigrationConstants.MIGRATION_ID;
import static org.nuxeo.rest.management.migration.MigrationConstants.MIGRATION_STATUS;

import java.io.IOException;
import java.util.List;

import org.nuxeo.ecm.core.io.marshallers.json.ExtensibleEntityJsonWriter;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;
import org.nuxeo.runtime.migration.Migration;
import org.nuxeo.runtime.migration.MigrationService.MigrationStatus;
import org.nuxeo.runtime.migration.MigrationStep;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @since 1.0.1
 */
@Setup(mode = SINGLETON, priority = REFERENCE)
public class MigrationJsonWriter extends ExtensibleEntityJsonWriter<Migration> {

    public MigrationJsonWriter() {
        super(MIGRATION_ENTITY_TYPE, Migration.class);
    }

    @Override
    public void writeEntityBody(Migration entity, JsonGenerator jg) throws IOException {
        jg.writeStringField(MIGRATION_ID, entity.getId());
        jg.writeStringField(MIGRATION_DESCRIPTION, entity.getDescription());
        jg.writeStringField(MIGRATION_DESCRIPTION_LABEL, entity.getDescriptionLabel());
        writeMigrationStatus(entity.getStatus(), jg);
        writeMigrationSteps(entity.getSteps(), jg);
    }

    protected void writeMigrationStatus(MigrationStatus status, JsonGenerator jg) throws IOException {
        jg.writeObjectFieldStart(MIGRATION_STATUS);
        jg.writeStringField("state", status.getState());
        jg.writeStringField("step", status.getStep());
        jg.writeNumberField("startTime", status.getStartTime());
        jg.writeNumberField("pingTime", status.getPingTime());
        jg.writeStringField("progressMessage", status.getProgressMessage());
        jg.writeNumberField("progressNum", status.getProgressNum());
        jg.writeNumberField("progressTotal", status.getProgressTotal());
        jg.writeBooleanField("isRunning", status.isRunning());
        jg.writeEndObject();
    }

    protected void writeMigrationSteps(List<MigrationStep> steps, JsonGenerator jg) throws IOException {
        jg.writeArrayFieldStart("steps");
        for (MigrationStep step : steps) {
            jg.writeStartObject();
            jg.writeStringField("id", step.getId());
            jg.writeStringField("fromState", step.getFromState());
            jg.writeStringField("toState", step.getToState());
            jg.writeStringField("description", step.getDescription());
            jg.writeStringField("descriptionLabel", step.getDescriptionLabel());
            jg.writeEndObject();
        }
        jg.writeEndArray();
    }

}
