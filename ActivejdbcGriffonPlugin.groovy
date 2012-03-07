/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by getApplication()licable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
 
/**
 * @author Andres Almiray
 */
class ActivejdbcGriffonPlugin {
    // the plugin version
    String version = '0.4'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '0.9.5 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [datasource: '0.3']
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-activejdbc-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Activejdbc support'
    def description = '''
The ActiveJdbc plugin enables lightweight access to MySql, Postgres, Oracle and H2 datasources using [Activejdbc][1].
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * DataSource.groovy - contains the datasource and pool definitions. Its format is equal to GORM's requirements.
 Additional configuration for this artifact is explained in the [DataSource Plugin][2].
 * BootstrapActivejdbc.groovy - defines init/destroy hooks for data to be manipulated during app startup/shutdown.

A new dynamic method named `withActivejdbc` will be injected into all controllers,
giving you access to a `org.javalite.activejdbc.Base` object, with which you'll be able
to make calls to the database. Remember to make all database calls off the EDT
otherwise your application may appear unresponsive when doing long computations
inside the EDT.
This method is aware of multiple databases. If no dataSourceName is specified when calling
it then the default database will be selected. Here are two example usages, the first
queries against the default database while the second queries a database whose name has
been configured as 'internal'

	package sample
	class SampleController {
	    def queryAllDatabases = {
	        withActivejdbc { dataSourceName -> ... }
	        withActivejdbc('internal') { dataSourceName -> ... }
	    }
	}
	
This method is also accessible to any component through the singleton `griffon.plugins.activejdbc.ActivejdbcConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`ActivejdbcEnhancer.enhance(metaClassInstance, activejdbcProviderInstance)`.

Configuration
-------------
### Dynamic method injection

The `withActivejdbc()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.activejdbc.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * ActivejdbcConnectStart[dataSourceName, dataSource] - triggered before connecting to the database
 * ActivejdbcConnectEnd[dataSourceName, dataSource] - triggered after connecting to the database
 * ActivejdbcDisconnectStart[dataSourceName, dataSource] - triggered before disconnecting from the database
 * ActivejdbcDisconnectEnd[dataSourceName, dataSource] - triggered after disconnecting from the database

This plugin relies on the facilities exposed by the [datasource][2] plugin.

### Example

A trivial sample application can be found at [https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/activejdbc][3]

Scripts
-------

 * **activejdbc-instrument** - collects information of model classes and enhances their bytecode with ActiveJdbc specific calls.

This script must be called explicitly before running the application **at least once**. If any of the model classes is updated
then you must clean your sources and call this script again.
	
**Don't forget to instrument the code, otherwise the model classes will not be picked up by ActiveJdbc.**

Testing
-------
The `withActivejdbc()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `ActivejdbcEnhancer.enhance(metaClassInstance, activejdbcProviderInstance)` where 
`activejdbcProviderInstance` is of type `griffon.plugins.activejdbc.ActivejdbcProvider`. The contract for this interface looks like this

    public interface ActivejdbcProvider {
        Object withActivejdbc(Closure closure);
        Object withActivejdbc(String dataSourceName, Closure closure);
        <T> T withActivejdbc(CallableWithArgs<T> callable);
        <T> T withActivejdbc(String dataSourceName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyActivejdbcProvider implements ActivejdbcProvider {
        Object withActivejdbc(String dataSourceName = 'default', Closure closure) { null }
        public <T> T withActivejdbc(String dataSourceName = 'default', CallableWithArgs<T> callable) { null }      
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            ActivejdbcEnhancer.enhance(service.metaClass, new MyActivejdbcProvider())
            // exercise service methods
        }
    }


[1]: http://code.google.com/p/activejdbc
[2]: /plugin/datasource
[3]: https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/activejdbc
'''
}
