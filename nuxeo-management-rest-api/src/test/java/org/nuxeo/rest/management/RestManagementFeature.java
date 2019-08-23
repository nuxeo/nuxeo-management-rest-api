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

import static org.nuxeo.launcher.config.ConfigurationGenerator.PARAM_HTTP_PORT;

import org.nuxeo.ecm.webengine.test.WebEngineFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RunnerFeature;
import org.nuxeo.runtime.test.runner.ServletContainerFeature;

/**
 * @since 11.1
 */
@Features(WebEngineFeature.class)
@Deploy("org.nuxeo.ecm.platform.web.common")
@Deploy("org.nuxeo.rest.management")
@Deploy("org.nuxeo.rest.management:OSGI-INF/test-webengine-servletcontainer-contrib.xml")
public class RestManagementFeature implements RunnerFeature {

    @Override
    public void start(FeaturesRunner runner) {
        // TODO this should be set by ServletContainerFeature itself
        int port = runner.getFeature(ServletContainerFeature.class).getPort();
        Framework.getProperties().put(PARAM_HTTP_PORT, String.valueOf(port));
    }
}
