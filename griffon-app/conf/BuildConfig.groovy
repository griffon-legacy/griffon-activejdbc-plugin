griffon.project.dependency.resolution = {
    // inherit Griffon' default dependencies
    inherits "global"
    log "warn" 
    repositories {
        griffonHome()
        mavenCentral()
        mavenLocal()
        mavenRepo 'https://oss.sonatype.org/content/repositories/snapshots/'
        mavenRepo 'http://ipsolutionsdev.com/snapshots/'
    }

    dependencies {
        String activejdbcVersion = '1.4.6'
        compile("org.javalite:activejdbc:$activejdbcVersion") {
            excludes 'junit', 'servlet-api', 'slf4j-api', 'jcl-over-slf4j', 'slf4j-simple'
        }
        compile("org.javalite:javalite-common:$activejdbcVersion") {
            excludes 'jaxen', 'junit'
        }
        build("org.javalite:javalite-common:$activejdbcVersion") {
            excludes 'jaxen', 'junit'
        }
        build("org.javalite:activejdbc-instrumentation:$activejdbcVersion")
        build('org.eclipse.jdt:org.eclipse.jdt.core:3.6.0.v_A58') {
            export = false
        }
        String lombokIdea = '0.5'
        build("de.plushnikov.lombok-intellij-plugin:processor-api:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:processor-core:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:intellij-facade-factory:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:intellij-facade-api:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:intellij-facade-9:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:intellij-facade-10:$lombokIdea",
              "de.plushnikov.lombok-intellij-plugin:intellij-facade-11:$lombokIdea") {
            export = false
            transitive = false
        }
        String ideaVersion = '11.1.4'
        build("org.jetbrains.idea:idea-openapi:$ideaVersion",
              "org.jetbrains.idea:extensions:$ideaVersion",
              "org.jetbrains.idea:util:$ideaVersion",
              "org.jetbrains.idea:annotations:$ideaVersion") {
            export = false
        }
    }
}

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon',
          'org.springframework',
          'org.apache.karaf',
          'groovyx.net'
    warn  'griffon'
}