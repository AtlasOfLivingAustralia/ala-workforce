import org.codehaus.groovy.grails.commons.ConfigurationHolder

eventWebXmlStart = {
    if (!ConfigurationHolder.config.security.cas.bypass) {
        def tmpWebXml = "${projectWorkDir}/web.xml.tmp"
        ant.replace(file: tmpWebXml, token: "@security.cas.serverName@", value: ConfigurationHolder.config.security.cas.serverName)
        println "Injecting CAS Security Configuration: serverName = ${ConfigurationHolder.config.security.cas.serverName}"
        ant.replace(file: tmpWebXml, token: "@security.cas.contextPath@", value: ConfigurationHolder.config.security.cas.contextPath)
        println "Injecting CAS Security Configuration: contextPath = ${ConfigurationHolder.config.security.cas.contextPath}"
        ant.replace(file: tmpWebXml, token: "/substitute-me", value: ConfigurationHolder.config.security.cas.urlPattern)
        println "Injecting CAS Security Configuration: url pattern = ${ConfigurationHolder.config.security.cas.urlPattern}"
    }
}
