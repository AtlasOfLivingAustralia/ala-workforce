// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

/******************************************************************************\
 *  SECURITY
\******************************************************************************/
if (!security.cas.urlPattern) {
    security.cas.urlPattern = "/.+"
}
if (!security.cas.urlExclusionPattern) {
    security.cas.urlExclusionPattern = "/images.*,/css.*,/js.*"
}
if (!security.cas.authenticateOnlyIfLoggedInPattern) {
    security.cas.authenticateOnlyIfLoggedInPattern = "/"
}
if (!security.cas.casServerName) {
    security.cas.casServerName = "https://auth.ala.org.au"
}
if (!security.cas.loginUrl) {
    security.cas.loginUrl = "${security.cas.casServerName}/cas/login"
}
if (!security.cas.logoutUrl) {
    security.cas.logoutUrl = "${security.cas.casServerName}/cas/logout"
}
if (!security.cas.contextPath) {
    security.cas.contextPath = "/workforce" //"""${appName}"
}
if (!security.cas.bypass) {
    security.cas.bypass = false
}
// SEE ENV SECTION for environment-specific overrides

/* end SECURITY */

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []


// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://abrs.ala.org.au/${appName}"
        security.cas.appServerName = "http://abrs.ala.org.au"
    }
    development {
        grails.serverURL = "http://woodfired.ala.org.au:8090/${appName}"
        security.cas.appServerName = "http://woodfired.ala.org.au:8090"
    }
    testserver {
        grails.serverURL = "http://testweb1.ala.org.au:8080/${appName}"
        security.cas.appServerName = "http://testweb1.ala.org.au:8080"
    }
    test {
        grails.serverURL = "http://localhost:8090/${appName}"
        security.cas.appServerName = "http://woodfired.ala.org.au:8090"
    }
}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.apache.commons',
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}
