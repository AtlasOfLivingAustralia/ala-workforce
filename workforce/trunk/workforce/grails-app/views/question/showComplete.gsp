<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>${qset.title}</title>
    </head>
    <body>
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

          <p>Thank you ${request.userPrincipal.attributes?.firstname}.
            <g:link controller="report" action="answers" id="${user}" params="${[set: qset.setId]}">Click here review yours answers</g:link>
          </p>
        </div>

        <div class="go-buttons">
            <a href="${ConfigurationHolder.config.grails.serverURL}"><img src="${resource(dir:'images/abrsskin',file:'home-button.png')}"/></a>
        </div>

    </body>
</html>