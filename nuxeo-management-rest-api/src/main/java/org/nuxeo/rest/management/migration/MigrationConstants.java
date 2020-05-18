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

/**
 * @since 1.0.1
 */
public final class MigrationConstants {

    public static final String MIGRATION_ENTITY_TYPE = "migration";

    public static final String MIGRATION_LIST_ENTITY_TYPE = "migrations";

    public static final String MIGRATION_ID = "id";

    public static final String MIGRATION_DESCRIPTION = "description";

    public static final String MIGRATION_DESCRIPTION_LABEL = "descriptionLabel";

    public static final String MIGRATION_STATUS = "status";

    public static final String MIGRATION_STEPS = "steps";

    private MigrationConstants() {
        // constants class
    }

}
