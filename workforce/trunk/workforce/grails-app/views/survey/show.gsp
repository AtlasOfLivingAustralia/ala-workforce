<%@ page import="au.org.ala.workforce.Survey" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'survey.label', default: 'Survey')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
    </span>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label"
                                                                           args="[entityName]"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
                                                                               args="[entityName]"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="default.show.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="survey.year.label" default="Survey Year:"/></td>

                <td valign="top" class="value">${fieldValue(bean: surveyInstance, field: "year")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="survey.id.label" default="QSet Id:"/></td>

                <td valign="top" class="value">${fieldValue(bean: surveyInstance, field: "id")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="survey.priorYear1.label" default="Prior Year1:"/></td>

                <td valign="top" class="value">${fieldValue(bean: surveyInstance, field: "priorYear1")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="survey.priorYear2.label" default="Prior Year2:"/></td>

                <td valign="top" class="value">${fieldValue(bean: surveyInstance, field: "priorYear2")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="survey.type.label" default="Type:"/></td>

                <td valign="top" class="value">${surveyInstance?.type?.encodeAsHTML()}</td>

            </tr>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form>
            <g:hiddenField name="id" value="${surveyInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="edit"
                                                 value="${message(code: 'default.button.edit.label', default: 'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" action="delete"
                                                 value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                                 onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
