<%@ page import="au.org.ala.workforce.User; au.org.ala.workforce.QuestionModel" %>
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
            <h1>ABRS Workforce Survey Administration</h1>
            <h2>${qset.title}</h2>
            <p></p>
            <table class='dashboard'>
                <tr>
                    <th>Surveys Started</th>
                    <th>Surveys Completed</th>
                </tr>
                <tr>
                    <td>85</td>
                    <td>24</td>
                </tr>
            </table>

            <p><g:link controller="report" action="answers" params="${[set:qset.setId,id:user]}"><strong>Show all answers</strong></g:link></p>
            <p><a href="${createLink(uri: '/')}">CSV Download</a></p>
            <p><a href="${createLink(uri: '/')}">Generate Charts</a></p>

            <p style="margin-bottom: 5px;">List of respondents</p>
            <ul class="respondents">
            <g:each in="${User.list(sort:'name')}" var="u">
                <li><g:link controller="report" action="answers" params="${[set:qset.setId,id:u.userid]}">${u.name}</g:link></li>
            </g:each>
            </ul>

            <g:if test="${qset.setId == 2}">
                <p style="margin-bottom: 5px;">List of collections</p>
                <ul class="respondents">
                    <li><g:link controller="report" action="answers" params="${[set:qset.setId,id:21]}">Australian Museum</g:link></li>
                    <li><g:link controller="report" action="answers" params="${[set:qset.setId,id:21]}">Tasmanian Museum and Gallery</g:link></li>
                    <li><g:link controller="report" action="answers" params="${[set:qset.setId,id:21]}">Museum Victoria</g:link></li>
                    <li><g:link controller="report" action="answers" params="${[set:qset.setId,id:21]}">Australian National Wildlife Collection</g:link></li>
                    <li><g:link controller="report" action="answers" params="${[set:qset.setId,id:21]}">South Australian Museum</g:link></li>
                </ul>
            </g:if>

        </div>
    </body>
</html>
