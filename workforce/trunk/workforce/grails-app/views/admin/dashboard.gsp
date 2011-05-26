<%@ page import="au.org.ala.workforce.QuestionModel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>ABRS Survey Administration</title>
    </head>
    <body>
        <div class="nav">
            <span class="navButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
        </div>
        <div class='links'>
            <h1>ABRS Workforce Survey Administration</h1>
            <h2>${qset.title}</h2>
            <p></p>
            <table class='dashboard'>
                <tr>
                    <th>No of Users</th>
                    <th>Started</th>
                    <th>Completed</th>
                </tr>
                <tr>
                    <td>128</td>
                    <td>85</td>
                    <td>24</td>
                </tr>
            </table>
            <p><g:link controller="report" action="answers" params="${[set:qset.setId,id:user]}"><strong>Show respondents</strong></g:link></p>
            <p><a href="${createLink(uri: '/')}">CSV Download</a></p>
            <p><a href="${createLink(uri: '/')}">Generate Charts</a></p>
        </div>
        <div class="buttons">
        </div>
    </body>
</html>
