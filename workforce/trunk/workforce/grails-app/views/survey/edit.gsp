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
    <span class="navButton"><a href="${createLink(uri: '/survey/show/' + surveyInstance?.id)}">Show Survey</a></span>
    <span class="navButton">Edit Survey</span>
</div>

<div class="body">
    <h1>Edit ABRS Survey Metadata</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${surveyInstance}">
        <div class="errors">
            <g:renderErrors bean="${surveyInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <g:hiddenField name="id" value="${surveyInstance?.id}"/>
        <g:hiddenField name="version" value="${surveyInstance?.version}"/>
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="year"><g:message code="survey.year.label" default="Year"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'year', 'errors')}">
                        <g:textField name="year" value="${surveyInstance.year}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="priorYear1"><g:message code="survey.priorYear1.label"
                                                           default="Prior Year1"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'priorYear1', 'errors')}">
                        <g:textField name="priorYear1"
                                     value="${surveyInstance.priorYear1}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="priorYear2"><g:message code="survey.priorYear2.label"
                                                           default="Prior Year2"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'priorYear2', 'errors')}">
                        <g:textField name="priorYear2"
                                     value="${surveyInstance.priorYear2}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="type"><g:message code="survey.type.label" default="Type"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'type', 'errors')}">
                        <g:select name="type" from="${au.org.ala.workforce.SurveyType?.values()}"
                                  keys="${au.org.ala.workforce.SurveyType?.values()*.name()}"
                                  value="${surveyInstance?.type?.name()}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div style="margin-top: 20px">
            <g:actionSubmit class="navButton" action="update" value="Update" style="color: #666; font-size: 12px"/>
        </div>
    </g:form>
</div>
</body>
</html>
