<%@ page import="au.org.ala.workforce.Survey" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'survey.label', default: 'Survey')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
    </span>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label"
                                                                           args="[entityName]"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="default.create.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${surveyInstance}">
        <div class="errors">
            <g:renderErrors bean="${surveyInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="questionSetId"><g:message code="survey.questionSetId.label"
                                                              default="Question Set Id"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'questionSetId', 'errors')}">
                        <g:textField name="questionSetId"
                                     value="${fieldValue(bean: surveyInstance, field: 'questionSetId')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="priorYear1"><g:message code="survey.priorYear1.label"
                                                           default="Prior Year1"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'priorYear1', 'errors')}">
                        <g:textField name="priorYear1"
                                     value="${fieldValue(bean: surveyInstance, field: 'priorYear1')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="priorYear2"><g:message code="survey.priorYear2.label"
                                                           default="Prior Year2"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'priorYear2', 'errors')}">
                        <g:textField name="priorYear2"
                                     value="${fieldValue(bean: surveyInstance, field: 'priorYear2')}"/>
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

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="year"><g:message code="survey.year.label" default="Year"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'year', 'errors')}">
                        <g:textField name="year" value="${fieldValue(bean: surveyInstance, field: 'year')}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:submitButton name="create" class="save"
                                                 value="${message(code: 'default.button.create.label', default: 'Create')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
