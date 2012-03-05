<%@ page import="au.org.ala.workforce.SurveyType; au.org.ala.workforce.Institution; au.org.ala.workforce.User; au.org.ala.workforce.Survey; au.org.ala.workforce.Event" %>
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
                                <g:if test="${qset.setId == Survey.getCurrentQSetId(SurveyType.personal) && completed > 2}">
                                    ${String.format('%.1f',(completed*100)/total)}%
                                </g:if>
                            </div>
                            <g:if test="${qset.setId == Survey.getCurrentQSetId(SurveyType.institutional) && completed < 3}">
                                <span style="float:left; padding-left: 3px;">${String.format('%.1f',(completed*100)/total)}%</span>
                            </g:if>
                            <div title="${started-completed} started but not completed" style="width: ${(started*100)/total}%"></div>
                        </div>
                        <span class="total">${total}</span>
                        <div style="clear:both"></div>

                        <g:set var="users" value="${User.getUsersWithAnswers(qset.setId, year as int)}"/>
                        <g:if test="${qset.setId == Survey.getCurrentQSetId(SurveyType.personal)}">
                            <h3>Respondents</h3>
                            <p style="margin-top: 5px;">Click a name to show answers.</p>
                            <ul class="respondents">
                            <g:each in="${users}" var="u">
                                <li><g:link controller="report" action="answers" params="${[set:qset.setId,id:u.userid]}">${u.name}</g:link>
                                <g:if test="${!Event.isComplete(qset.setId, u.userid, year as int)}">
                                    <span style="color: red">(incomplete)</span>
                                </g:if>
                                </li>
                            </g:each>
                            </ul>
                        </g:if>

                        <g:if test="${qset.setId == Survey.getCurrentQSetId(SurveyType.institutional)}">
                            <h3>Institutions</h3>
                            <p style="margin-top: 5px;">Click a name to show answers.</p>
                            <ul class="respondents">
                                <g:each in="${Institution.listInstitutionsForSet(qset.setId)}" var="i">
                                    <g:set var="userid" value="${users.find{it.name == i.account}?.userid}"/>
                                    <g:if test="${userid}">
                                        <li><g:link controller="report" action="answers" params="${[set:qset.setId,id:userid]}">${i.name}</g:link>
                                        <g:if test="${!Event.isComplete(qset.setId, userid, year as int)}">
                                            <span style="color: red">(incomplete)</span>
                                        </g:if>
                                        </li>
                                    </g:if>
                                    <g:else>
                                        <li class="dull">${i.name}</li>
                                    </g:else>
                                </g:each>
                            </ul>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${users.empty}">
                            <p class="dull">Browse all answers</p>
                            <p class="dull">CSV Download</p>
                        </g:if>
                        <g:else>
                            <p><g:link controller="report" action="answers" params="${[set:qset.setId]}"><strong>Browse all answers</strong></g:link></p>
                            <p><g:link controller="download" action="download" params="${[set:qset.setId]}"><strong>CSV Download</strong></g:link></p>
                            <chart:jsonCharts survey="${qset.shortName}"/>
                        </g:else>
                    </td>
                </tr>
            </table>

        </div>
    </body>
</html>
