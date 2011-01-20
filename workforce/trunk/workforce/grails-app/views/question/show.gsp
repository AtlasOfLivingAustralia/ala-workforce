<%@ page import="au.org.ala.workforce.Question" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'question.label', default: 'Question')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.level1.label" default="Level1" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "level1")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.level2.label" default="Level2" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "level2")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.level3.label" default="Level3" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "level3")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.label.label" default="Label" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "label")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.qtext.label" default="Qtext" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "qtext")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.qtype.label" default="Qtype" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "qtype")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.qdata.label" default="Qdata" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "qdata")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.instruction.label" default="Instruction" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "instruction")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.atype.label" default="Atype" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "atype")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.alabel.label" default="Alabel" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "alabel")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.adata.label" default="Adata" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "adata")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="question.displayHint.label" default="Display Hint" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: questionInstance, field: "displayHint")}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${questionInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
