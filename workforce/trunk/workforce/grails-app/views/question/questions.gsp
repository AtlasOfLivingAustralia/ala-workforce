<%@ page import="au.org.ala.workforce.QuestionModel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>${qset.title}</title>
    </head>
    <body>
        <g:form action="submit">
            <g:hiddenField name="set" value="${qset.setId}"/>
            <g:hiddenField name="from" value="${pagination.from}"/>
            <g:hiddenField name="to" value="${pagination.to}"/>
            <g:hiddenField name="totalPages" value="${pagination.totalPages}"/>
            <g:hiddenField name="pageNumber" value="${pagination.pageNumber}"/>
            <div class="nav">
                <span class="navButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
                <g:if test="${pagination.pageNumber != 1}">
                    <g:actionSubmit class="navButton" action="previous" value="Prev" />
                </g:if>
                <g:else>
                    <!-- this is an invisible placeholder for the prev button on subsequent pages -->
                    <input style="visibility:hidden;" type="submit" disabled="true" value="Prev" class="navButton"/>
                </g:else>
                <span class='pageProgress'><wf:pageProgress set="${qset.setId}" page="${pagination.pageNumber}" total="${pagination.totalPages}"/></span>
                <g:if test="${pagination.pageNumber != pagination.totalPages}">
                    <g:actionSubmit class="navButton" action="next" value="Next" />
                </g:if>
                <g:if test="${pagination.pageNumber == pagination.totalPages}">
                    <g:actionSubmit class="navButton" action="finish" value="Finish" />
                </g:if>
            </div>
            <div class="body">
                <g:if test="${questions.size() == 1}">
                    <h1>${qset.title} - Question ${pagination.from}</h1>
                </g:if>
                <g:else>
                    <h1>${qset.title} - Questions ${pagination.from} to ${pagination.to}</h1>
                </g:else>
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

              <div class="dialog">
                  <table class="questions">
                      <colgroup><col width="4%"><col width="33%"><col width="70%"></colgroup>
                      <tbody>

                        <g:each var='question' in="${questions}">
                          <wf:question question="${question}"/>
                          <tr><td colspan='3'></td></tr>
                        </g:each>

                      </tbody>
                  </table>
              </div>
            </g:form>
        </div>

    </body>
</html>
