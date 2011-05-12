<%@ page import="au.org.ala.workforce.QuestionSet" %>
<html>
    <head>
        <title>ABRS Surveys</title>
        <meta name="layout" content="main" />
        <style type="text/css" media="screen">
            div.right {
                float: right;
            }
            div.right img {
                padding-right: 40px;
                padding-top: 30px;
            }
            div.links {
                float: left;
                padding-top: 40px;
                padding-left: 80px;
            }
            p {
                margin-top: 10px;
                margin-bottom: 10px;
            }
            li {
                padding: 5px;
            }

        </style>

    </head>
    <body>
        <div style="padding-bottom: 300px;" class='links'>
            <h1>ABRS Workforce Surveys</h1>
            <p>Click a link to begin a survey.</p>
            <ul>
                <g:each var="qs" in="${QuestionSet.list([sort:'setId'])}">
                    <li><g:link controller="question" action="page" params='[set:"${qs.setId}",page:1]'>${qs.title}</g:link></li>
                </g:each>
            </ul>
        </div>
    </body>
</html>
