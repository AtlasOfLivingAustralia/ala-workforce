<%@ page import="au.org.ala.workforce.QuestionSet" %>
<html>
    <head>
        <title>ABRS Surveys</title>
        <meta name="layout" content="main" />
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
        <g:if test="${admin}">
            <div class="go-buttons">
                <g:link controller="admin" action="dashboard" params='[set:1]'><img src="${resource(dir:'images/abrsskin',file:'personal-admin.png')}"/></g:link>
                <g:link controller="admin" action="dashboard" params='[set:2]'><img src="${resource(dir:'images/abrsskin',file:'collections-admin.png')}"/></g:link>
            </div>
        </g:if>
        <g:else>
            <div class="go-buttons">
                <g:link controller="question" action="page" params='[set:1,page:1]'><img src="${resource(dir:'images/abrsskin',file:'personal-button.png')}"/></g:link>
                <g:link controller="question" action="page" params='[set:2,page:1]'><img src="${resource(dir:'images/abrsskin',file:'collections-button.png')}"/></g:link>
            </div>
        </g:else>

        <g:if test="${admin}">
            <p><a href="${createLink(uri: '/')}">Home</a></p>
        </g:if>
        <g:else>
            <p><g:link controller="admin" action="index">Dashboard</g:link></p>
        </g:else>

    </body>
</html>
