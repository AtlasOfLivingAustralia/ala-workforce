<%@ page import="au.org.ala.workforce.Institution; au.org.ala.workforce.Survey; au.org.ala.workforce.SurveyType;" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>${qset.title}</title>
    </head>
    <body>
        <div class="nav" id="breadcrumb">
            <wf:isABRSAdmin>
                <span class="navButton"><a class="home" href="${createLink(uri: '/admin')}">Home</a></span>
                <span class="navButton"><a href="${createLink(uri: '/admin/set/' + qset.setId)}">${qset.shortName}</a></span>
            </wf:isABRSAdmin>
            <wf:isNotABRSAdmin>
                <span class="navButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            </wf:isNotABRSAdmin>
            <span class="navButton">${user.name}</span>
        </div>
        <div class="body">
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:if test="${user}">
                <g:if test="${qset.setId == Survey.getCurrentQSetId(SurveyType.personal)}">
                    <g:if test="${user.firstName && user.lastName}">
                        <div style='float: left; padding: 25px 0px'>Answers for ${user.firstName} ${user.lastName}</div>
                    </g:if>
                    <g:else>
                        <div style='float: left; padding: 25px 0px'>Answers for ${user.name}</div>
                    </g:else>
                </g:if>
                <g:if test="${qset.setId == Survey.getCurrentQSetId(SurveyType.institutional)}">
                    <div style='float: left; padding: 25px 0px'>Answers for ${Institution.findByAccount(user.name).name}</div>
                </g:if>
                <div style='float: left; padding: 25px'><wf:summaryStatus user="${user}"/></div>
                <g:if test="${request.isUserInRole('ROLE_ABRS_ADMIN') || request.isUserInRole('ROLE_ADMIN')}">
                    <wf:reportNavigation users= "${users}" user="${user}"/>
                </g:if>
            </g:if>
            <div style='clear: both;'>
                <table class="answers">
                    <colgroup><col width="4%"><col width="50%"><col width="46%"></colgroup>
                    <tbody>

                    <g:each var='question' in="${questions}">
                      <wf:report question="${question}" qset="${qset}"/>
                    </g:each>

                    </tbody>
                </table>
            </div>
            <g:if test="${request.isUserInRole('ROLE_ABRS_ADMIN') || request.isUserInRole('ROLE_ADMIN')}">
                <wf:reportNavigation users= "${users}" user="${user}"/>
            </g:if>
        </div>
    </body>
</html>
