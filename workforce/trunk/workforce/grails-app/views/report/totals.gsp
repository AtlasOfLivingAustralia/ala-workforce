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
        </div>
        <div class="body">
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
             <div style='clear: both;'>
                <table class="answers">
                    <colgroup><col width="4%"><col width="50%"><col width="46%"></colgroup>
                    <tbody>

                    <g:each var='question' in="${questions}">
                      <wf:totals question="${question}" qset="${qset}"/>
                    </g:each>

                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
