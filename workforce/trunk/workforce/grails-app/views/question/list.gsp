
<%@ page import="au.org.ala.workforce.Question" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'question.label', default: 'Question')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'question.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="level1" title="${message(code: 'question.level1.label', default: 'Level1')}" />
                        
                            <g:sortableColumn property="level2" title="${message(code: 'question.level2.label', default: 'Level2')}" />
                        
                            <g:sortableColumn property="level3" title="${message(code: 'question.level3.label', default: 'Level3')}" />
                        
                            <g:sortableColumn property="label" title="${message(code: 'question.label.label', default: 'Label')}" />
                        
                            <g:sortableColumn property="qtext" title="${message(code: 'question.qtext.label', default: 'Qtext')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${questionInstanceList}" status="i" var="questionInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${questionInstance.id}">${fieldValue(bean: questionInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: questionInstance, field: "level1")}</td>
                        
                            <td>${fieldValue(bean: questionInstance, field: "level2")}</td>
                        
                            <td>${fieldValue(bean: questionInstance, field: "level3")}</td>
                        
                            <td>${fieldValue(bean: questionInstance, field: "label")}</td>
                        
                            <td>${fieldValue(bean: questionInstance, field: "qtext")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${questionInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
