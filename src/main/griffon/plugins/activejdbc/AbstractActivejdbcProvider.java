/*
 * Copyright 2012-2013 the original author or authors.
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

package griffon.plugins.activejdbc;

import griffon.plugins.datasource.DataSourceHolder;
import griffon.util.CallableWithArgs;
import groovy.lang.Closure;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractActivejdbcProvider implements ActivejdbcProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractActivejdbcProvider.class);
    private static final String DEFAULT = "default";

    public <R> R withActivejdbc(Closure<R> closure) {
        return withActivejdbc(DEFAULT, closure);
    }

    public <R> R withActivejdbc(String dataSourceName, Closure<R> closure) {
        if (isBlank(dataSourceName)) dataSourceName = DEFAULT;
        if (closure != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on processEngine '" + dataSourceName + "'");
            }
            try {
                Base.open(DataSourceHolder.getInstance().getDataSource(dataSourceName));
                return closure.call(dataSourceName);
            } finally {
                Base.close();
            }
        }
        return null;
    }

    public <R> R withActivejdbc(CallableWithArgs<R> callable) {
        return withActivejdbc(DEFAULT, callable);
    }

    public <R> R withActivejdbc(String dataSourceName, CallableWithArgs<R> callable) {
        if (isBlank(dataSourceName)) dataSourceName = DEFAULT;
        if (callable != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on processEngine '" + dataSourceName + "'");
            }
            try {
                Base.open(DataSourceHolder.getInstance().getDataSource(dataSourceName));
                callable.setArgs(new Object[]{dataSourceName});
                return callable.call();
            } finally {
                Base.close();
            }
        }
        return null;
    }
}