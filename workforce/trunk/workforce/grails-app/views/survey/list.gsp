<%@ page import="au.org.ala.workforce.Survey" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'survey.label', default: 'Survey')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
    </span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
                                                                               args="[entityName]"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="id" title="${message(code: 'survey.id.label', default: 'Id')}"/>

                <g:sortableColumn property="questionSetId"
                                  title="${message(code: 'survey.questionSetId.label', default: 'Question Set Id')}"/>

                <g:sortableColumn property="priorYear1"
                                  title="${message(code: 'survey.priorYear1.label', default: 'Prior Year1')}"/>

                <g:sortableColumn property="priorYear2"
                                  title="${message(code: 'survey.priorYear2.label', default: 'Prior Year2')}"/>

                <g:sortableColumn property="type" title="${message(code: 'survey.type.label', default: 'Type')}"/>

                <g:sortableColumn property="year" title="${message(code: 'survey.year.label', default: 'Year')}"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${surveyInstanceList}" status="i" var="surveyInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show"
                                id="${surveyInstance.id}">${fieldValue(bean: surveyInstance, field: "id")}</g:link></td>

                    <td>${fieldValue(bean: surveyInstance, field: "questionSetId")}</td>

                    <td>${fieldValue(bean: surveyInstance, field: "priorYear1")}</td>

                    <td>${fieldValue(bean: surveyInstance, field: "priorYear2")}</td>

                    <td>${fieldValue(bean: surveyInstance, field: "type")}</td>

                    <td>${fieldValue(bean: surveyInstance, field: "year")}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${surveyInstanceTotal}"/>
    </div>
</div>
</body>
</html>
