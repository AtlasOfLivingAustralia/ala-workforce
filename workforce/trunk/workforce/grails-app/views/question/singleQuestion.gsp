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
            <h1>Question ${question.questionNumber}</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div>
                <table class="questions">
                    <colgroup><col width="4%"><col width="35%"><col width="61%"></colgroup>
                    <tbody>

                    <wf:question question="${question}"/>


                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${question.questionNumber}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="Save" /></span>
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="Submit" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="cancel" value="Cancel" onclick="return confirm('${message(code: 'default.button.cancel.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
