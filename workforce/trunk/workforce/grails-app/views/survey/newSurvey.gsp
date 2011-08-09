<%@ page import="au.org.ala.workforce.Survey" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>ABRS Survey Administration</title>
    </head>
    <body>
        <div class="nav" id="breadcrumb">
            <span class="navButton"><a class="home" href="${createLink(uri: '/admin')}"><g:message code="default.home.label"/></a></span>
            <span class="navButton"><a href="${createLink(uri: '/admin/set/' + qset.setId)}">${qset.shortName}</a></span>
            <span class="navButton">Create New Survey</span>
        </div>
        <div class='links'>
            <h1>ABRS Taxonomic Workforce Survey Creation</h1>
            <h2>${qset.title}</h2>
            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
            <g:set var="surveyType" value="${Survey.getType(qset.setId)}"/>
            <g:form action="save">
                <div class="dialog">
                    <g:hiddenField name="surveyType" value="${surveyType}"/>
                    <g:hiddenField name="set" value="${qset.setId}"/>
                    <table>
                        <tbody>

                        <tr class="prop">
                            <td valign="top" class="name">
                                <label for="year">Survey Year:</label>
                            </td>

                            <g:if test="${surveyInstance.year == 0}">
                                <g:set var="surveyYearValue" value=""/>
                            </g:if>
                            <g:else>
                                <g:set var="surveyYearValue" value="${surveyInstance.year}"/>
                            </g:else>

                            <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'year', 'errors')}">
                                <g:textField name="year" value="${surveyYearValue}"/>
                            </td>
                        </tr>

                        <g:if test="${surveyType == 'personal'}">
                            <g:set var="priorYearLabel" value="Funding Year"/>
                        </g:if>
                        <g:elseif test="${surveyType == 'institutional'}">
                            <g:set var="priorYearLabel" value="Publications Year"/>
                        </g:elseif>

                        <tr class="prop">
                            <td valign="top" class="name">
                                <label for="priorYear1">${priorYearLabel}1:</label>
                            </td>

                            <g:if test="${surveyInstance.priorYear1 == 0}">
                                <g:set var="priorYear1Value" value=""/>
                            </g:if>
                            <g:else>
                                <g:set var="priorYear1Value" value="${surveyInstance.priorYear1}"/>
                            </g:else>

                            <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'priorYear1', 'errors')}">
                                <g:textField name="priorYear1" value="${priorYear1Value}"/>
                            </td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name">
                                <label for="priorYear2">${priorYearLabel}2:</label>
                            </td>

                            <g:if test="${surveyInstance.priorYear2 == 0}">
                                <g:set var="priorYear2Value" value=""/>
                            </g:if>
                            <g:else>
                                <g:set var="priorYear2Value" value="${surveyInstance.priorYear2}"/>
                            </g:else>

                            <td valign="top" class="value ${hasErrors(bean: surveyInstance, field: 'priorYear2', 'errors')}">
                                <g:textField name="priorYear2" value="${priorYear2Value}"/>
                            </td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name">
                                <label for="basedOn">Base survey on:</label>
                            </td>

                            <g:set var="optionsList" value="${['Blank Survey','2003 Survey','2006 Survey','2011 Survey']}"/>
                            <td valign="top" class="value">
                                <g:select name="basedOn" from="${optionsList}" value=""/>
                            </td>
                        </tr>

                        </tbody>
                    </table>
                </div>

                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save"
                                                         value="${message(code: 'default.button.create.label', default: 'Create')}"/></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
