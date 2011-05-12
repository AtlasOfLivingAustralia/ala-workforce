<%@ page import="au.org.ala.workforce.QuestionModel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>${qset.title}</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class='pageProgress'><wf:pageProgress set="${qset.setId}" page="${pagination.pageNumber}" total="${pagination.totalPages}"/></span>
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
            <g:form action="submit">
              <g:hiddenField name="set" value="${qset.setId}"/>
              <g:hiddenField name="from" value="${pagination.from}"/>
              <g:hiddenField name="to" value="${pagination.to}"/>
              <g:hiddenField name="totalPages" value="${pagination.totalPages}"/>
              <g:hiddenField name="pageNumber" value="${pagination.pageNumber}"/>
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
              <div class="buttons">
                  <span class="button"><g:actionSubmit class="edit" action="edit" value="Save" /></span>
                  <span class="button"><g:actionSubmit class="edit" action="submit" value="Submit" /></span>
                  <!-- this is temp - to allow nav without validation -->
                  <g:if test="${pagination.pageNumber != 1}">
                      <span class="button"><g:actionSubmit class="edit" action="previous" value="Prev" /></span>
                  </g:if>
                  <g:if test="${pagination.pageNumber != pagination.totalPages}">
                      <span class="button"><g:actionSubmit class="edit" action="next" value="Next" /></span>
                  </g:if>

                  <span class="button"><g:actionSubmit class="delete" action="cancel" value="Cancel"
                     onclick="return confirm('${message(code: 'default.button.cancel.confirm.message',
                     default: 'All answers will be lost. Are you sure?')}');" /></span>
              </div>
            </g:form>
        </div>

    </body>
</html>
