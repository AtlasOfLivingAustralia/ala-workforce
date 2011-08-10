<%@ page import="au.org.ala.workforce.Survey" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'survey.label', default: 'Survey')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav" id="breadcrumb">
    <span class="navButton"><a class="home" href="${createLink(uri: '/admin')}"><g:message code="default.home.label"/></a></span>
    <span class="navButton">Survey List</span>
</div>

<div class="body">
    <h1>ABRS Taxonomic Workforce Survey List</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="id" title="${message(code: 'survey.id.label', default: 'Question Set Id')}"/>

                <g:sortableColumn property="year" title="${message(code: 'survey.year.label', default: 'Survey Year')}"/>

                <g:sortableColumn property="priorYear1"
                                  title="${message(code: 'survey.priorYear1.label', default: 'Prior Year1')}"/>

                <g:sortableColumn property="priorYear2"
                                  title="${message(code: 'survey.priorYear2.label', default: 'Prior Year2')}"/>

                <g:sortableColumn property="type" title="${message(code: 'survey.type.label', default: 'Type')}"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${surveyInstanceList}" status="i" var="surveyInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show"
                                id="${surveyInstance.id}">${surveyInstance.id}</g:link></td>

                    <td>${surveyInstance.year}</td>

                    <td>${surveyInstance.priorYear1}</td>

                    <td>${surveyInstance.priorYear2}</td>

                    <td>${surveyInstance.type}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

</div>
</body>
</html>
