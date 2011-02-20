<%@ page import="au.org.ala.workforce.QuestionModel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Show single question</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
        </div>
        <div class="body">
          <h1>Questions</h1>
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
              <g:hiddenField name="from" value="${from}"/>
              <g:hiddenField name="to" value="${to}"/>
              <div class="dialog">
                  <table class="questions">
                      <colgroup><col width="4%"><col width="33%"><col width="70%"></colgroup>
                      <tbody>

                        <g:each var='questionNumber' in="${from..to}">
                          <wf:question questionNumber="${questionNumber}"/>
                          <tr><td colspan='3'></td></tr>
                        </g:each>

                      </tbody>
                  </table>
              </div>
              <div class="buttons">
                      <span class="button"><g:actionSubmit class="edit" action="edit" value="Save" /></span>
                      <span class="button"><g:actionSubmit class="edit" action="submit" value="Submit" /></span>
                      <span class="button"><g:actionSubmit class="delete" action="cancel" value="Cancel" onclick="return confirm('${message(code: 'default.button.cancel.confirm.message', default: 'Are you sure?')}');" /></span>
              </div>
            </g:form>
        </div>
    </body>
</html>
