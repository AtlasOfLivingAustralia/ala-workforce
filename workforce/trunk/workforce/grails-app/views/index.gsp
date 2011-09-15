<%@ page import="au.org.ala.workforce.SurveyType; org.codehaus.groovy.grails.commons.ConfigurationHolder; au.org.ala.workforce.QuestionSet; au.org.ala.workforce.Survey; au.org.ala.workforce.DateUtil" %>
<html>
    <head>
        <title>ABRS Surveys</title>
        <meta name="layout" content="main" />
    </head>
    <body>
        <g:set var="type" value="${wf.selectSurvey()}"/>
        <g:set var="currentYear" value="${DateUtil.getCurrentYear()}"/>
        <g:if test="${flash.message}"><div class="message">${flash.message}</div></g:if>

        <div class='links'>
            <g:if test="${type == 'personal'}">
                <h1>ABRS Taxonomic Capacity Survey</h1>
                <p>You are invited to participate in the ${currentYear} ABRS Taxonomic Capacity Survey.</p>
                <p>If you work in taxonomy in an Australian context, please complete the
                  <g:link controller="question" action="page" params='[set:1,page:1]'><strong>Australian Taxonomic Workforce - Personal Survey</strong></g:link>.</p>
                <p>The survey questions are presented over a number of pages. Your answers are saved each time you navigate away from a page
                (i.e. press 'Next' or 'Prev' or jump to a page). <em>Note that clicking the browser Back or Forward buttons will not save your answers</em>.</p>
                <p>You can partially fill in the survey, press 'Exit' and return to it at a later time.
                Click the 'Finish' link on the last page when you have completed the survey.</p>
                <p>Note that your session will expire after 2 hours of inactivity. If this happens you will be prompted to login again.</p>
            </g:if>
            <g:elseif test="${type == 'institution'}">
                <h1>ABRS Taxonomic Capacity Survey</h1>
                <p>You are invited to participate in the ${currentYear} ABRS Taxonomic Capacity Survey.</p>
                <p>If you are responsible for an Australian natural science collection, please complete the
                    <g:link controller="question" action="page" params='[set:2,page:1]'><strong>Institution Survey</strong></g:link>.</p>
                <p>The survey questions are presented over a number of pages. Your answers are saved each time you navigate away from a page
                (i.e. press 'Next' or 'Prev' or jump to a page). <em>Note that clicking the browser Back or Forward buttons will not save your answers</em>.</p>
                <p>You can partially fill in the survey, press 'Exit' and return to it at a later time.
                Click the 'Finish' link on the last page when you have completed the survey.</p>
                <p>Note that your session will expire after 2 hours of inactivity. If this happens you will be prompted to login again.</p>
            </g:elseif>
            <g:else>
                <h1>ABRS Taxonomic Capacity Surveys</h1>
                <p>You are invited to participate in the ${currentYear} ABRS Taxonomic Capacity Surveys.</p>
                <p>You need to log in to answer a survey.</p>
                <h2>Personal survey</h2>
                <p>You may use your existing logon for the Atlas of Living Australia or click the button below to register a new account.</p>
                <h2>Institutional survey</h2>
                <p>If you have been selected to complete an institutional survey, log in with the account that you have been sent.</p>
                <p>If you also wish to complete the personal survey, logout and log in again using your own personal account.</p>
            </g:else>
        </div>
        <g:if test="${admin}">
            <div class="go-buttons">
                <g:link controller="admin" action="dashboard" params='[set:1]'><img src="${resource(dir:'images/abrsskin',file:'personal-admin.png')}"/></g:link>
                <g:link controller="admin" action="dashboard" params='[set:2]'><img src="${resource(dir:'images/abrsskin',file:'collections-admin.png')}"/></g:link>
            </div>
        </g:if>
        <g:else>
            <div class="go-buttons">
            <g:if test="${type == 'institution'}">
                <g:link controller="question" action="page" params='[set:2, page:1]'>
                    <img src='${resource(dir:'images/abrsskin',file:'collections-button.png')}'/></g:link>
                <wf:surveyStatus setid="${Survey.getCurrentQSetId(SurveyType.institutional)}"/>
             </g:if>
            <g:elseif test="${type == 'personal'}">
                <g:link controller="question" action="page" params='[set:1, page:1]'>
                    <img src='${resource(dir:'images/abrsskin',file:'personal-button.png')}'/></g:link>
                <wf:surveyStatus setid="${Survey.getCurrentQSetId(SurveyType.personal)}"/>
            </g:elseif>
            <g:else>
                <a href="${ConfigurationHolder.config.security.cas.loginUrl}?service=${ConfigurationHolder.config.security.cas.appServerName}${ConfigurationHolder.config.security.cas.contextPath}/">
                    <img src='${resource(dir:'images/abrsskin',file:'login-button.png')}'/></a>
            </g:else>
            </div>
        </g:else>

        <g:if test="${admin}">
            <p><a href="${createLink(uri: '/')}">Home</a></p>
        </g:if>
        <g:else>
            <wf:isABRSAdmin>
                <p><g:link controller="admin" action="index">Dashboard</g:link></p>
            </wf:isABRSAdmin>
        </g:else>

    </body>
</html>
