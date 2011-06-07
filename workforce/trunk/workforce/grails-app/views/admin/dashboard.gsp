<%@ page import="au.org.ala.workforce.Institution; au.org.ala.workforce.User; au.org.ala.workforce.QuestionModel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>ABRS Survey Administration</title>
    </head>
    <body>
        <div class="nav" id="breadcrumb">
            <span class="navButton"><a class="home" href="${createLink(uri: '/admin')}"><g:message code="default.home.label"/></a></span>
            <span class="navButton">${qset.shortName}</span>
        </div>
        <div class='links'>
            <h1>ABRS Taxonomic Workforce Survey Administration</h1>
            <h2>${qset.title}</h2>
            <table class='dashboard'>
                <tr>
                    <td>
                        <table class='stats'>
                            <tr>
                                <th>Surveys Started</th>
                                <th>Surveys Completed</th>
                            </tr>
                            <tr>
                                <td>${started}</td>
                                <td>${completed}</td>
                            </tr>
                        </table>
                        <div class="progress-container">
                            <div title="${completed} completed" style="width: ${(completed*100)/total}%">
                                <g:if test="${qset.setId==2 && completed > 2}">
                                    ${String.format('%.1f',(completed*100)/total)}%
                                </g:if>
                            </div>
                            <g:if test="${qset.setId==2 && completed < 3}">
                                <span style="float:left; padding-left: 3px;">${String.format('%.1f',(completed*100)/total)}%</span>
                            </g:if>
                            <div title="${started-completed} started but not completed" style="width: ${(started*100)/total}%"></div>
                        </div>
                        <span class="total">${total}</span>
                        <div style="clear:both"></div>

                        <g:if test="${qset.setId == 1}">
                            <h3>Respondents</h3>
                            <p style="margin-top: 5px;">Click a name to show answers.</p>
                            <ul class="respondents">
                            <g:each in="${User.list(sort:'name')}" var="u">
                                <li><g:link controller="report" action="answers" params="${[set:qset.setId,id:u.userid]}">${u.name}</g:link></li>
                            </g:each>
                            </ul>
                        </g:if>

                        <g:if test="${qset.setId == 2}">
                            <h3>Institutions</h3>
                            <p style="margin-top: 5px;">Click a name to show answers.</p>
                            <ul class="respondents">
                                <g:each in="${Institution.findAllBySetId(qset.setId)}" var="i">
                                    <g:set var="userid" value="${User.findByName(i.account)?.userid}"/>
                                    <g:if test="${userid}">
                                        <li><g:link controller="report" action="answers" params="${[set:qset.setId,id:userid]}">${i.name}</g:link></li>
                                    </g:if>
                                    <g:else>
                                        <li class="dull">${i.name}</li>
                                    </g:else>
                                </g:each>
                            </ul>
                        </g:if>
                    </td>
                    <td>
                        <p><g:link controller="report" action="answers" params="${[set:qset.setId]}"><strong>Browse all answers</strong></g:link></p>
                        <p><g:link controller="download" action="download" params="${[set:qset.setId]}"><strong>CSV Download</strong></g:link></p>
                        <p><a href="${createLink(uri: '/')}">Generate Charts</a></p>
                    </td>
                </tr>
            </table>

        </div>
    </body>
</html>
