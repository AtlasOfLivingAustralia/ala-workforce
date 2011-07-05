<%@ page import="au.org.ala.workforce.QuestionModel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>${qset.title}</title>
    </head>
    <body>
        <g:form action="page" params="${[set: qset.setId, page: pagination.pageNumber]}">
            <g:hiddenField name="set" value="${qset.setId}"/>
            <g:hiddenField name="from" value="${pagination.from}"/>
            <g:hiddenField name="to" value="${pagination.to}"/>
            <g:hiddenField name="totalPages" value="${pagination.totalPages}"/>
            <g:hiddenField name="pageNumber" value="${pagination.pageNumber}"/>
            <div id="hiddenAction"></div>
            <div class="nav">
                <span class="navButton home"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
                <g:if test="${pagination.pageNumber != 1}">
                    <g:actionSubmit class="navButton prev" action="previous" value="Prev"/>
                </g:if>
                <g:else>
                    <!-- this is an invisible placeholder for the prev button on subsequent pages -->
                    <input style="visibility:hidden;" type="submit" disabled="true" value="Prev" class="navButton prev"/>
                </g:else>
                <span class='pageProgress'><wf:pageProgress set="${qset.setId}" page="${pagination.pageNumber}" total="${pagination.totalPages}"/></span>
                <g:if test="${pagination.pageNumber != pagination.totalPages}">
                    <g:actionSubmit class="navButton" action="next" value="Next"/>
                </g:if>
                <g:if test="${pagination.pageNumber == pagination.totalPages}">
                    <g:actionSubmit class="navButton" action="finish" value="Finish"/>
                </g:if>
            </div>
            <div class="body">
                <g:if test="${questions.size() == 1}">
                    <h1>${qset.title} - Question ${pagination.from}</h1>
                </g:if>
                <g:else>
                    <h1>${qset.title} - Questions ${pagination.from} to ${pagination.to}</h1>
                </g:else>
              <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
              </g:if>
              <g:if test="${errors}">
              <div class="errors">
                <ul>
                <g:each var="error" in="${errors}">
                  <li><wf:listError error="${error}"/></li>
                </g:each>
                </ul>
              </div>
              </g:if>

              <div class="dialog">
                  <table class="questions">
                      <colgroup><col width="4%"><col width="33%"><col width="70%"></colgroup>
                      <tbody>

                        <g:each var='question' in="${questions}" status="count">
                          <wf:question question="${question}"/>
                          <g:if test="${count < questions.size() - 1}">
                              <tr><td colspan='3' style='*height: 14px'></td></tr>
                          </g:if>
                        </g:each>

                      </tbody>
                  </table>
              </div>

            <div class="nav-bottom">
                <span class="navButton"></span>
                <g:if test="${pagination.pageNumber != 1}">
                    <g:actionSubmit class="navButton prevBottom" action="previous" value="Prev" />
                </g:if>
                <g:else>
                    <!-- this is an invisible placeholder for the prev button on subsequent pages -->
                    <input style="visibility:hidden;" type="submit" disabled="true" value="Prev" class="navButton prevBottom"/>
                </g:else>
                <span class='pageProgress'><wf:pageProgress set="${qset.setId}" page="${pagination.pageNumber}" total="${pagination.totalPages}"/></span>
                <g:if test="${pagination.pageNumber != pagination.totalPages}">
                    <g:actionSubmit class="navButton" action="next" value="Next"/>
                </g:if>
                <g:if test="${pagination.pageNumber == pagination.totalPages}">
                    <g:actionSubmit class="navButton" action="finish" value="Finish"/>
                </g:if>
            </div>
            </g:form>

        </div>

    <script type="text/javascript">
        function clearRadio(ident) {
            $('input[name='+ident+']:checked').removeAttr('checked');
        }
        function disable(ident, src) {
            if (src.value == 'yes' && src.checked) {
                $('input[name='+ident+']').removeAttr('disabled');
                $('#disabledState').attr('value','enabled');
            } else {
                $('input[name='+ident+']').removeAttr('checked');
                $('input[name='+ident+']').attr('disabled','true');
                $('#disabledState').attr('value','disabled');
             }
        }
        function setInitialState() {
            // find any input with onchange="disable(..)" and set the states accordingly
            $('input[onclick*="disable"][value="no"]').each(function(index,element) {
                var funct = this.getAttributeNode('onclick').value;
                var ident = funct.substring(funct.indexOf("'")+1,funct.lastIndexOf("',"));
                if ($(this).attr('checked') == 'checked') {
                    disable(ident, this);
                } else {
                    // make sure the hidden input state is enabled
                    $('#disabledState').attr('value','enabled');
                }
            });
        }
        $(document).ready(function() {
            setInitialState();
        });
        $(document).keypress(function(event) {
            if (event.which == 13) {
                <g:if test="${pagination.pageNumber != pagination.totalPages}">
                    $("#hiddenAction").append("<input type='hidden' name='_action_next' value='Next'/>")
                </g:if>
                <g:if test="${pagination.pageNumber == pagination.totalPages}">
                    $("#hiddenAction").append("<input type='hidden' name='_action_finish' value='Finish'/>")
                </g:if>
                $("form").submit()
            }
        });
    </script>
    </body>
</html>
