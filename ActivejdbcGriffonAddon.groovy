/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import griffon.core.GriffonClass
import griffon.core.GriffonApplication
import griffon.plugins.activejdbc.ActivejdbcConnector
import griffon.plugins.activejdbc.ActivejdbcEnhancer
import griffon.plugins.activejdbc.ActivejdbcContributionHandler

import static griffon.util.ConfigUtils.getConfigValueAsBoolean

/**
 * @author Andres Almiray
 */
class ActivejdbcGriffonAddon {
    void addonPostInit(GriffonApplication app) {
        if (getConfigValueAsBoolean(app.config, 'griffon.activejdbc.connect.onstartup', true)) {
            ActivejdbcConnector.instance.connect(app)
        }
        def types = app.config.griffon?.activejdbc?.injectInto ?: ['controller']
        for(String type : types) {
            for(GriffonClass gc : app.artifactManager.getClassesOfType(type)) {
                if (ActivejdbcContributionHandler.isAssignableFrom(gc.clazz)) continue
                ActivejdbcEnhancer.enhance(gc.metaClass)
            }
        }
    }

    Map events = [
        ShutdownStart: { app ->
            ActivejdbcConnector.instance.disconnect(app)
        }
    ]
}