<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>${qset.title}</title>
    </head>
    <body>
        <div class="nav" id="breadcrumb">
            <span class="navButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="navButton">Summary</span>
        </div>
        <div class="links">
          <h1>${qset.title}</h1>
          <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
          </g:if>
          <g:if test="${errors}">
          <div class="errors">
            <ul>
            <g:each var="error" in="${errors}">
              <li><wf:listError error="${error}"/></li>
            </g:each>
            </ul>
          </div>
          </g:if>

          <p>Thank you for completing the survey.</p>
          <p>You can still make changes to your answers if you wish.</p>
          <p class="textLinks">
            <g:link controller="report" action="answers" id="${user}" params="${[set: qset.setId]}">Click here to see a quick summary of your answers.</g:link><br/>
            <g:link action="page" params="${[set: qset.setId, page: 1]}">Click here to change your answers.</g:link>
          </p>
        </div>

    </body>
</html>