<%@ page import="au.org.ala.workforce.Config" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'config.label', default: 'Config')}"/>
    <title>Edit Survey Configuration</title>
</head>

<body>
<div class="nav" id="breadcrumb">
    <span class="navButton"><a class="home" href="${createLink(uri: '/admin')}"><g:message code="default.home.label"/></a></span>
    <span class="navButton">Configuration</span>
</div>

<div class="body">
    <h1>Edit Survey Configuration</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${configInstance}">
        <div class="errors">
            <g:renderErrors bean="${configInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <g:hiddenField name="id" value="${configInstance?.id}"/>
        <g:hiddenField name="version" value="${configInstance?.version}"/>
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="currentSurveyYear"><g:message code="config.currentSurveyYear.label"
                                                                  default="Current Survey Year"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: configInstance, field: 'currentSurveyYear', 'errors')}">
                        <g:textField name="currentSurveyYear" value="${configInstance.currentSurveyYear}"  size="10"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="feedbackEmail"><g:message code="config.feedbackEmail.label"
                                                              default="Feedback Email"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: configInstance, field: 'feedbackEmail', 'errors')}">
                        <g:textField name="feedbackEmail" value="${configInstance?.feedbackEmail}" size="30"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="supportEmail"><g:message code="config.supportEmail.label"
                                                             default="Support Email"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: configInstance, field: 'supportEmail', 'errors')}">
                        <g:textField name="supportEmail" value="${configInstance?.supportEmail}" size="30"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="">
            <g:actionSubmit class="navButton" action="update" value="Update" style="color: #666; font-size: 12px"/>
        </div>
    </g:form>
</div>
</body>
</html>
