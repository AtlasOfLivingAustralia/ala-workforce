<%@ page import="au.org.ala.workforce.Survey" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'survey.label', default: 'Survey')}"/>
    <title>ABRS Survey Administration</title>
</head>

<body>
<div class="nav" id="breadcrumb">
    <span class="navButton"><a class="home" href="${createLink(uri: '/admin')}"><g:message code="default.home.label"/></a></span>
    <span class="navButton"><a href="${createLink(uri: '/survey/list')}">Survey List</a></span>
    <span class="navButton">Show Survey</span>
</div>

<div class="body">
    <h1>Show ABRS Survey Metadata</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="survey.year.label" default="Survey Year:"/></td>

                <td valign="top" class="value">${surveyInstance.year}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="survey.id.label" default="QSet Id:"/></td>

                <td valign="top" class="value">${surveyInstance.id}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="survey.priorYear1.label" default="Prior Year1:"/></td>

                <td valign="top" class="value">${surveyInstance.priorYear1}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="survey.priorYear2.label" default="Prior Year2:"/></td>

                <td valign="top" class="value">${surveyInstance.priorYear2}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="survey.type.label" default="Type:"/></td>

                <td valign="top" class="value">${surveyInstance?.type?.encodeAsHTML()}</td>

            </tr>

            </tbody>
        </table>
    </div>

    <div style="margin-top: 20px">
        <g:form>
            <g:hiddenField name="id" value="${surveyInstance?.id}"/>
            <g:actionSubmit class="navButton" action="edit" value="Edit" style="color: #666; font-size: 12px"/>
            <g:actionSubmit class="navButton" action="delete" value="Delete" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" style="color: #666; font-size: 12px"/>
        </g:form>
    </div>
</div>
</body>
</html>
