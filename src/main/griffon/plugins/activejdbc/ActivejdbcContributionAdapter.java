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

import griffon.util.CallableWithArgs;
import groovy.lang.Closure;

/**
 * @author Andres Almiray
 */
public class ActivejdbcContributionAdapter implements ActivejdbcContributionHandler {
    private static final String DEFAULT = "default";

    private ActivejdbcProvider provider = DefaultActivejdbcProvider.getInstance();

    public void setActivejdbcProvider(ActivejdbcProvider provider) {
        this.provider = provider != null ? provider : DefaultActivejdbcProvider.getInstance();
    }

    public ActivejdbcProvider getActivejdbcProvider() {
        return provider;
    }

    public <R> R withActivejdbc(Closure<R> closure) {
        return withActivejdbc(DEFAULT, closure);
    }

    public <R> R withActivejdbc(String dataSourceName, Closure<R> closure) {
        return provider.withActivejdbc(dataSourceName, closure);
    }

    public <R> R withActivejdbc(CallableWithArgs<R> callable) {
        return withActivejdbc(DEFAULT, callable);
    }

    public <R> R withActivejdbc(String dataSourceName, CallableWithArgs<R> callable) {
        return provider.withActivejdbc(dataSourceName, callable);
    }
}