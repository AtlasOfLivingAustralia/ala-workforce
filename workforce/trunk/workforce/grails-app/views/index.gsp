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
                padding-top: 40px;
                padding-left: 80px;
                padding-bottom: 30px;            }
            p {
                margin-top: 15px;
                margin-bottom: 15px;
                font-size: 12px;
            }
            li {
                padding: 5px;
            }
            div.go-buttons {
                padding-bottom: 100px;
                text-align: center;
            }
            div.go-buttons img {
                padding: 0 20px 0 20px;
            }

        </style>

    </head>
    <body>
        <div class='links'>
            <h1>ABRS Workforce Surveys</h1>
            <p>You are invited to participate in the 2011 ABRS taxonomic resources surveys.</p>
            <p>If you work in taxonomy in an Australian context, please complete the
              <g:link controller="question" action="page" params='[set:1,page:1]'><strong>Australian Taxonomic Workforce - Personal Survey</strong></g:link>.</p>
            <p>If you are responsible for an Australian natural science collection, please complete the
            <g:link controller="question" action="page" params='[set:2,page:1]'><strong>Resources of Australian Natural Science Collections Survey</strong></g:link>.</p>
        </div>
        <div class="go-buttons">
            <g:link controller="question" action="page" params='[set:1,page:1]'><img src="${resource(dir:'images/abrsskin',file:'personal-button.png')}"/></g:link>
            <g:link controller="question" action="page" params='[set:2,page:1]'><img src="${resource(dir:'images/abrsskin',file:'collections-button.png')}"/></g:link>
        </div>
    </body>
</html>
