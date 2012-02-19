griffon.project.dependency.resolution = {
    // inherit Griffon' default dependencies
    inherits "global"
    log "warn" 
    repositories {
        griffonHome()
        mavenCentral()
        mavenRepo 'https://oss.sonatype.org/content/repositories/snapshots/'
        mavenRepo 'http://ipsolutionsdev.com/snapshots/'
    }
    
    def activejdbcVersion = '1.2-SNAPSHOT'
    
    dependencies {
        compile("org.javalite:activejdbc:$activejdbcVersion") {
            excludes 'junit', 'servlet-api', 'slf4j-api', 'jcl-over-slf4j', 'slf4j-simple'
        }
        build("org.javalite:javalite-common:$activejdbcVersion") {
            excludes 'jaxen'
        }
        build("org.javalite:activejdbc-instrumentation:$activejdbcVersion")
    }
}

griffon {
    doc {
        logo = '<a href="http://griffon.codehaus.org" target="_blank"><img alt="The Griffon Framework" src="../img/griffon.png" border="0"/></a>'
        sponsorLogo = "<br/>"
        footer = "<br/><br/>Made with Griffon (@griffon.version@)"
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