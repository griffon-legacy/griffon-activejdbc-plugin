/*
 * Copyright 2011-2013 the original author or authors.
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
    String version = '1.0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.2.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [datasource: '1.1.0']
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
The ActiveJdbc plugin enables lightweight access to MySql, Postgres, Oracle
and H2 datasources using [Activejdbc][1]. This plugin does NOT provide domain
classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * DataSource.groovy - contains the datasource and pool definitions. Its format
   is equal to GORM's requirements. Additional configuration for this artifact
   is explained in the [Activejdbc Plugin][2].
 * BootstrapActivejdbc.groovy - defines init/destroy hooks for data to be
   manipulated during app startup/shutdown.

A new dynamic method named `withActivejdbc` will be injected into all controllers,
giving you access to a `org.javalite.activejdbc.Base` object, with which you'll
be able to make calls to the repository. Remember to make all repository calls
off the UI thread otherwise your application may appear unresponsive when doing
long computations inside the UI thread.

This method is aware of multiple dataSources. If no dataSourceName is specified
when calling it then the default dataSource will be selected. Here are two
example usages, the first queries against the default dataSource while the second
queries a dataSource whose name has been configured as 'internal'

    package sample
    class SampleController {
        def queryAllDatabases = {
            withActivejdbc { dataSourceName -> ... }
            withActivejdbc('internal') { dataSourceName -> ... }
        }
    }

The following list enumerates all the variants of the injected method

 * `<R> R withActivejdbc(Closure<R> stmts)`
 * `<R> R withActivejdbc(CallableWithArgs<R> stmts)`
 * `<R> R withActivejdbc(String dataSourceName, Closure<R> stmts)`
 * `<R> R withActivejdbc(String dataSourceName, CallableWithArgs<R> stmts)`

These methods are also accessible to any component through the singleton
`griffon.plugins.activejdbc.ActivejdbcEnhancer`. You can inject these methods to
non-artifacts via metaclasses. Simply grab hold of a particular metaclass and
call `ActivejdbcEnhancer.enhance(metaClassInstance)`.

Configuration
-------------
### ActivejdbcAware AST Transformation

The preferred way to mark a class for method injection is by annotating it with
`@griffon.plugins.activejdbc.ActivejdbcAware`. This transformation injects the
`griffon.plugins.activejdbc.ActivejdbcContributionHandler` interface and default
behavior that fulfills the contract.

### Dynamic Method Injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.activejdbc.injectInto = ['controller', 'service']

Dynamic method injection will be skipped for classes implementing
`griffon.plugins.activejdbc.ActivejdbcContributionHandler`.

### Events

The following events will be triggered by this addon

 * ActivejdbcConnectStart[dataSourceName, dataSource] - triggered before
   connecting to the database
 * ActivejdbcConnectEnd[dataSourceName, dataSource] - triggered after
   connecting to the database
 * ActivejdbcDisconnectStart[dataSourceName, dataSource] - triggered before
   disconnecting from the database
 * ActivejdbcDisconnectEnd[dataSourceName, dataSource] - triggered after
   disconnecting from the database

This plugin relies on the facilities exposed by the [datasource][2] plugin.

### Example

A trivial sample application can be found at [https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/activejdbc][3]

Scripts
-------

 * **activejdbc-instrument** - collects information of model classes and
   enhances their bytecode with ActiveJdbc specific calls.

This script must be called explicitly before running the application **at least
once**. If any of the model classes are updated then you must clean compiled
sources and call this script again.

**Don't forget to instrument the code, otherwise the model classes will not be
picked up by ActiveJdbc.**

Testing
-------

Dynamic methods will not be automatically injected during unit testing, because
addons are simply not initialized for this kind of tests. However you can use
`ActivejdbcEnhancer.enhance(metaClassInstance, activejdbcProviderInstance)` where
`activejdbcProviderInstance` is of type `griffon.plugins.activejdbc.ActivejdbcProvider`.
The contract for this interface looks like this

    public interface ActivejdbcProvider {
        <R> R withActivejdbc(Closure<R> closure);
        <R> R withActivejdbc(CallableWithArgs<R> callable);
        <R> R withActivejdbc(String dataSourceName, Closure<R> closure);
        <R> R withActivejdbc(String dataSourceName, CallableWithArgs<R> callable);
    }

It's up to you define how these methods need to be implemented for your tests.
For example, here's an implementation that never fails regardless of the
arguments it receives

    class MyActivejdbcProvider implements ActivejdbcProvider {
        public <R> R withActivejdbc(Closure<R> closure) { null }
        public <R> R withActivejdbc(CallableWithArgs<R> callable) { null }
        public <R> R withActivejdbc(String dataSourceName, Closure<R> closure) { null }
        public <R> R withActivejdbc(String dataSourceName, CallableWithArgs<R> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            ActivejdbcEnhancer.enhance(service.metaClass, new MyActivejdbcProvider())
            // exercise service methods
        }
    }

On the other hand, if the service is annotated with `@ActivejdbcAware` then usage
of `ActivejdbcEnhancer` should be avoided at all costs. Simply set
`activejdbcProviderInstance` on the service instance directly, like so, first the
service definition

    @griffon.plugins.activejdbc.ActivejdbcAware
    class MyService {
        def serviceMethod() { ... }
    }

Next is the test

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            service.activejdbcProvider = new MyActivejdbcProvider()
            // exercise service methods
        }
    }

Tool Support
------------

### DSL Descriptors

This plugin provides DSL descriptors for Intellij IDEA and Eclipse (provided
you have the Groovy Eclipse plugin installed). These descriptors are found
inside the `griffon-activejdbccompile-x.y.z.jar`, with locations

 * dsdl/activejdbc.dsld
 * gdsl/activejdbc.gdsl

### Lombok Support

Rewriting Java AST in a similar fashion to Groovy AST transformations is
possible thanks to the [lombok][4] plugin.

#### JavaC

Support for this compiler is provided out-of-the-box by the command line tools.
There's no additional configuration required.

#### Eclipse

Follow the steps found in the [Lombok][4] plugin for setting up Eclipse up to
number 5.

 6. Go to the path where the `lombok.jar` was copied. This path is either found
    inside the Eclipse installation directory or in your local settings. Copy
    the following file from the project's working directory

         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/activejdbc<version>/dist/griffon-activejdbccompile-<version>.jar .

 6. Edit the launch script for Eclipse and tweak the boothclasspath entry so
    that includes the file you just copied

        -Xbootclasspath/a:lombok.jar:lombok-pg-<version>.jar:\
        griffon-lombok-compile-<version>.jar:griffon-activejdbccompile-<version>.jar

 7. Launch Eclipse once more. Eclipse should be able to provide content assist
    for Java classes annotated with `@ActivejdbcAware`.

#### NetBeans

Follow the instructions found in [Annotation Processors Support in the NetBeans
IDE, Part I: Using Project Lombok][5]. You may need to specify
`lombok.core.AnnotationProcessor` in the list of Annotation Processors.

NetBeans should be able to provide code suggestions on Java classes annotated
with `@ActivejdbcAware`.

#### Intellij IDEA

Follow the steps found in the [Lombok][4] plugin for setting up Intellij IDEA
up to number 5.

 6. Copy `griffon-activejdbccompile-<version>.jar` to the `lib` directory

         $ pwd
           $USER_HOME/Library/Application Support/IntelliJIdea11/lombok-plugin
         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/activejdbc<version>/dist/griffon-activejdbccompile-<version>.jar lib

 7. Launch IntelliJ IDEA once more. Code completion should work now for Java
    classes annotated with `@ActivejdbcAware`.


[1]: http://code.google.com/p/activejdbc
[2]: /plugin/datasource
[3]: https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/activejdbc
[4]: /plugin/lombok
[5]: http://netbeans.org/kb/docs/java/annotations-lombok.html
'''
}
