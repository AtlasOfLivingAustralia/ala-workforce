<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>${qset.title}</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
        </div>
        <div class="body">
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

          <p>Thank you ${request.userPrincipal.attributes?.firstname}.</p>
        </div>
    </body>
</html>