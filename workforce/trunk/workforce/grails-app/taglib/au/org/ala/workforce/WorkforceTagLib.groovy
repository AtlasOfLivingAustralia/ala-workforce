package au.org.ala.workforce

import au.org.ala.cas.util.AuthenticationCookieUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import java.text.NumberFormat
import org.jasig.cas.client.authentication.AttributePrincipal

/**
 * Notes:
 *
 * radio buttons must have class='radio' to style background color and border as IE6 doesn't handle attribute selectors
 * checkboxes must have class='checkbox' to style background color and border as IE6 doesn't handle attribute selectors
 */

class WorkforceTagLib {

    static namespace = 'wf'

    def modelLoaderService, listLoaderService

    /**
     * Return html for a single question.
     *
     * One of the first two attrs must be present.
     *
     * @attr questionNumber the number of the question
     * @attr question the actual question model
     */
    def question = { attrs ->
        QuestionModel model = attrs.question

        setDisabledState(model)

        List secondLevel = model.questions
        int secondLevelIndex = 0

        // pre-calculate whether the first cell needs to span all rows
        int firstCellRowSpan = cell1RowSpan(model)

        /*
         * The top level may:
         *  - just be a container for subquestions (no qtext and no answer)
         *  - just be the carrier for the question text (qtext but no answer)
         *  - be a real question (both qtext and answer)
         *  - (?) take an answer but have no question text (no qtext but answer)
         * This determines top-level layout.
         */
        if (model.atype == AnswerType.none) {
            // there is no answer but there may be text
            if (model.qtext) {
                // question text should span q & a columns
                out << "<tr>"
                  out << "<td rowspan='${firstCellRowSpan}' id='Q${model.questionNumber}'>Q${model.questionNumber}</td>"
                  out << "<td colspan=2>${buildText(model)}</td>"
                out << "</tr>"

            } else {
                // go straight to first sub-question
                out << "<tr>"
                  out << "<td rowspan='${firstCellRowSpan}' id='Q${model.questionNumber}'>Q${model.questionNumber}</td>"
                if (secondLevel) {
                  QuestionModel q = secondLevel[secondLevelIndex++]
                  out << "<td>${buildText(q)}</td>"
                  out << "<td>" + layoutWidget(q) + "</td>"
                } else {
                  out << "<td></td><td></td>"
                }
                out << "</tr>"

            }
        } else {
            if (model.qtext) {
                // real question - with text and answer
                out << "<tr>"
                  out << "<td rowspan='${firstCellRowSpan}' id='Q${model.questionNumber}'>Q${model.questionNumber}</td>"
                  out << "<td>${buildText(model)}</td>"
                  out << "<td>" + layoutWidget(model) + "</td>"
                out << "</tr>"

            } else {
                // probably won't occur
                out << "<tr>"
                  out << "<td rowspan='${firstCellRowSpan}' id='Q${model.questionNumber}'>Q${model.questionNumber}</td>"
                  out << "<td colspan=2>" + layoutWidget(model) + "</td>"
                out << "</tr>"
            }
        }

        // second and third level questions
        /**
         * Sub-questions may be repeating.
         *
         * A single sub-question can:
         *  span the entire 2 column space; or
         *  have text in the left column and a widget in the right; or
         * 
         */
        for (secondLevelIndex; secondLevelIndex < secondLevel.size(); secondLevelIndex++) {

            QuestionModel q = secondLevel[secondLevelIndex]

            def content = generateContentFor(q)

            // new row
            out << "<tr>"

            // add cell with optional label unless the very first cell is spanning all rows
            if (firstCellRowSpan == 1) {
                out << "<td>${makeLabel(q)}</td>"
            }

            // add cells 2 and 3
            if (content.spanColumns2and3) {
                out << "<td colspan=2>" + content.secondColumnHtml + "</td>"
            } else {
                //println "secondColumnHtml = ${content.secondColumnHtml}"
                def style = q.qdata?.align ? " style='text-align:${q.qdata.align};'" : ''
                out << "<td${style}>" + content.secondColumnHtml + "</td><td>" + content.thirdColumnHtml + "</td>"
            }

            // end row
            out << "</tr>"
        }

        // instruction
        if (model.instruction && model.instructionPosition == 'bottom') {
            // add in bottom row
            out << "<tr>"
                out << "<td colspan=2><span class='instruction'>${model.instruction}</span></td>"
            out << "</tr>"
        }
    }

    /**
     * Return html for the answers report for a single question.
     *
     * @attr question the actual question model
     */
    def report = { attrs ->
        QuestionModel model = attrs.question
        QuestionSet qset = attrs.qset
        def user = params.id

        List secondLevel = model.questions
        int secondLevelIndex = 0

        // second and third level questions
        /**
         * Sub-questions may be repeating.
         *
         * A single sub-question can:
         *  span the entire 2 column space; or
         *  have text in the left column and a widget in the right; or
         *
         */
        def contents = []
        for (secondLevelIndex; secondLevelIndex < secondLevel.size(); secondLevelIndex++) {

            QuestionModel q = secondLevel[secondLevelIndex]

            if (areAnswers(q)) {
                contents << generateContentForReport(q, user)
            }
        }

        // calculate row span for 1st column
        int firstCellRowSpan = contents.size();

        /*
         * The top level may:
         *  - just be a container for subquestions (no qtext and no answer)
         *  - just be the carrier for the question text (qtext but no answer)
         *  - be a real question (both qtext and answer)
         *  - (?) take an answer but have no question text (no qtext but answer)
         * This determines top-level layout.
         */
        if (model.atype == AnswerType.none) {
            // there is no answer but there may be text
            if (model.qtext) {
                // question text should span q & a columns only if there are answers
                firstCellRowSpan++
                out << "<tr>"
                    out << "<td rowspan='${firstCellRowSpan}'>${getQuestionLink(model, qset, user)}</td>"
                    if (firstCellRowSpan > 1) {
                        out << "<td colspan='2'>${getQuestionTextForReport(model, user)}</td>"
                    } else {
                        out << "<td>${getQuestionTextForReport(model, user)}</td><td/>"
                    }
                out << "</tr>"

            } else {
                // go straight to first sub-question
                out << "<tr>"
                    out << "<td rowspan='${firstCellRowSpan}'>${getQuestionLink(model, qset, user)}</td>"
                if (secondLevel) {
                    QuestionModel q = secondLevel[0]
                    if (contents) {contents.remove(0)}
                    out << "<td>${getQuestionTextForReport(q, user)}</td>"
                    out << "<td>" + layoutAnswer(q) + "</td>"
                } else {
                    out << "<td></td><td></td>"
                }
                out << "</tr>"

            }
        } else {
            if (model.qtext) {
                // real question - with text and answer
                firstCellRowSpan++
                out << "<tr>"
                    out << "<td rowspan='${firstCellRowSpan}'>${getQuestionLink(model, qset, user)}</td>"
                    out << "<td>${getQuestionTextForReport(model, user)}</td>"
                    out << "<td>" + layoutAnswer(model) + "</td>"
                out << "</tr>"

            } else {
                // probably won't occur
                out << "<tr>"
                    out << "<td rowspan='${firstCellRowSpan}'>${getQuestionLink(model, qset, user)}</td>"
                    out << "<td colspan=2>" + layoutAnswer(model) + "</td>"
                out << "</tr>"
            }
        }

        contents.each {
            out << "<tr>"

//            QuestionModel q = it.question

            // add cell with optional label unless the very first cell is spanning all rows
//            if (firstCellRowSpan == 1) {
//                out << "<td>${makeLabel(q)}</td>"
//            }

            // add cells 2 and 3
            if (it.spanColumns2and3) {
                out << "<td colspan=2>" + it.secondColumnHtml + "</td>"
            } else {
                //println "secondColumnHtml = ${contents.secondColumnHtml}"
                def style = '' //q.qdata?.align ? " style='text-align:${q.qdata.align};'" : ''
                out << "<td${style}>" + it.secondColumnHtml + "</td><td>" + it.thirdColumnHtml + "</td>"
            }

            out << "</tr>"
        }
    }

    private Map generateContentFor(QuestionModel q) {
        Map result = [secondColumnHtml: "", thirdColumnHtml: "", spanColumns2and3: false]
        /*
         * Calculate the content of second and third columns
         * -------------------------------------------------
         *
         * If the qtype is matrix, group or rank then the content spans both columns
         *
         * Otherwise:
         * If there are no 3rd level questions then layout is:
         * || question text || answer widget ||
         * unless there is no question text in which case:
         * || answer widget ||
         *
         * If there are 2nd & 3rd level questions the layout is:
         * || question text & answer widget (order determined by type || 3rd level questions and widgets ||
         */
        if (q.qtype == QuestionType.matrix) {
            result = q.atype == AnswerType.radio ? layoutRadioMatrix(q) : layoutMatrix(q)
        }

        else if (q.qtype == QuestionType.group) {
            result = layoutGroup(q)
        }

        else if (q.qtype == QuestionType.rank) {
            result = layoutRank(q)
        }

        else if (q.questions) {
            // all level 2 in column 2; level 3 in column 3
            if (q.displayHint == 'checkbox') {
                // widget first
                result.secondColumnHtml = layoutWidget(q) + " ${buildText(q)}"
            } else {
                // text first
                result.secondColumnHtml = "${buildText(q)} " + layoutWidget(q)
            }
            // 3rd level questions
            result.thirdColumnHtml = layoutLevel3(q)

        }

        else {
            // no 3rd level questions - put widgets in 3rd column unless there is no content for the 2nd
            if (q.qtext) {
                result.secondColumnHtml = buildText(q)
                result.thirdColumnHtml = layoutWidget(q)
            } else {
                result.secondColumnHtml = layoutWidget(q)
                result.spanColumns2and3 = true
            }
        }
        return result
    }

    private Map generateContentForReport(QuestionModel q, String userId) {
        Map result = [secondColumnHtml: "", thirdColumnHtml: "", spanColumns2and3: false, question: q]
        /*
         * Calculate the content of second and third columns
         * -------------------------------------------------
         *
         * If the qtype is matrix, group or rank then the content spans both columns
         *
         * Otherwise:
         * If there are no 3rd level questions then layout is:
         * || question text || answer widget ||
         * unless there is no question text in which case:
         * || answer widget ||
         *
         * If there are 2nd & 3rd level questions the layout is:
         * || question text & answer widget (order determined by type || 3rd level questions and widgets ||
         */
        if (q.qtype == QuestionType.matrix) {
            result = q.atype == AnswerType.radio ? layoutRadioMatrixForReport(q) : layoutMatrixForReport(q, userId)
        }

        else if (q.qtype == QuestionType.group) {
            result = layoutGroupForReport(q, userId)
        }

        else if (q.qtype == QuestionType.rank) {
            result = layoutRankForReport(q)
        }

        else if (q.questions) {
            // all level 2 in column 2; level 3 in column 3
            if (q.displayHint == 'checkbox') {
                // widget first
                result.secondColumnHtml = "${getQuestionTextForReport(q, userId)}"
            } else {
                // text first
                def questionText = getQuestionTextForReport(q, userId)
                def answerText = layoutAnswer(q)
                result.secondColumnHtml = "${questionText} ${answerText}"
            }

            // 3rd level questions
            result.thirdColumnHtml = layoutLevel3ForReport(q, userId)
        }

        else {
            // no 3rd level questions - put widgets in 3rd column unless there is no content for the 2nd
            if (q.qtext) {
                result.secondColumnHtml = getQuestionTextForReport(q, userId)
                result.thirdColumnHtml = layoutAnswer(q)
            } else {
                result.thirdColumnHtml = layoutAnswer(q)
            }
        }
        return result
    }

    private String layoutWidget(QuestionModel q) {
        String result = ''

        switch (q.atype) {
            case AnswerType.bool:
                def yesChecked = (q.answerValueStr?.toLowerCase() in ['yes', 'true', 'on']) ? 'checked' : ''
                def noChecked = (q.answerValueStr?.toLowerCase() in ['no', 'false']) ? 'checked' : ''
                // hack for IE - use onclick instead
                def onchange = q.onchangeAction ? "onclick=${q.onchangeAction}()" : ""
                if (q.displayHint == 'checkbox') {
                    result += "<input ${onchange} class='checkbox' type='checkbox' name='${q.ident()}' ${yesChecked}/>"
                } else {
                    result += "<input ${onchange} class='radio' class='radio' type='radio' name='${q.ident()}' id='yes' value='yes' ${yesChecked}/><label for='yes'>Yes</label>"
                    result += "<input ${onchange} class='radio' type='radio' name='${q.ident()}' id='no' value='no' ${noChecked}/><label for='no'>No</label>"
                }
                break
            case AnswerType.none:
                break
            case AnswerType.number:
                def answer = q.answerValueStr?.isNumber() ? NumberFormat.getInstance().format(q.answerValueStr.toDouble()) : q.answerValueStr
                //println "answer str is ${q.answerValueStr}; formatted answer is ${answer}"
                def atts = [name: q.ident(), size: 8, value: answer, 'class': 'number']
                if (q.onchangeAction == 'updateSum') {
                    atts.put 'class', 'number summable'
                }
                result += textField(atts) + " " + (q.alabel ?: "")
                break
            case AnswerType.summable:
                def answer = q.answerValueStr?.isNumber() ? NumberFormat.getInstance().format(q.answerValueStr.toDouble()) : q.answerValueStr
                def atts = [name: q.ident(), size: 8, value: answer]
                def noOfCols = q.owner.qdata.cols.size()
                def rowRange = q.owner.adata.rowRange ?: [start: 1, finish: Integer.MAX_VALUE]
                def colRange = q.owner.adata.colRange ?: [start: 1, finish: Integer.MAX_VALUE]
                def rowCol = getRowAndColumnNumber(q.ident(), noOfCols)
                if (rowCol.rowNum >= (rowRange.start as Integer) && rowCol.rowNum <= (rowRange.finish as Integer) &&
                    rowCol.colNum >= (colRange.start as Integer) && rowCol.colNum <= (colRange.finish as Integer)) {
                    atts.put 'class', 'summable number'
                    atts.put 'colnum', rowCol.colNum
                } else {
                    atts.put 'class', 'number'
                }
                def sumRow = q.owner.adata.sumRow
                if (sumRow && sumRow as int == rowCol.rowNum) {
                    result += hiddenField(name: q.ident(), value: answer, totalnum: rowCol.colNum, class: 'intratotal') + "<span class='calculated' id='s${q.ident()}' totalnum='${rowCol.colNum}'>${answer?:''}</span>"
                } else {
                    result += textField(atts) + " " + (q.alabel ?: "")
                }
                break
            case AnswerType.radio:
                // display radio buttons with text from adata to select one chunk of text
                if (q.requiredIf == "enabled") {
                    result += "<input type='hidden' name='disabledState' id='disabledState' value='" +
                            (q.isDisabled() ? "disabled" : "enabled") + "'/>"
                }
                def items = []
                q.adata.eachWithIndex { it, idx ->
                    def checked = it == q.answerValueStr ? 'checked' : ''
                    // use escaped " for value so that apostrophe is handled correctly
                    items << "<input class='radio' type='radio' name='${q.ident()}' id='${q.ident()}_${idx}' value=\"${it}\" ${checked}/><label for='${q.ident()}_${idx}'>${it}</label>"
                }
                if (items.size() == 2) {
                    // treat as boolean with arbitrary labels
                    result += items.join()
                }
                else {
                    result += layoutListOfItems(items, 7)
                }
                break
            case AnswerType.text:
                def size = extractTextFieldSize(q.displayHint)
                Map params = [name: q.ident(), size: size, value: q.answerValueStr]
                result += textField(params) + " " + (q.alabel ?: "")
                break
            case AnswerType.textarea:
                result += textArea(name: q.ident(), rows: q.adata?.rows ?: 4, value: (q.answerValueStr ?: "")) + " " + (q.alabel ?: "")
                break
            case AnswerType.percent:
                def params = [name: q.ident(), size: 7, value: q.answerValueStr]
                if (q.onchangeAction == 'updateSum') {
                    params.put 'class', 'summable'
                }
                if (q.level == 2 && q.layoutHint == 'align-with-level3') {  //TODO: apply to other types
                    result = "<div class='alignWithLevel3'>" + textField(params) + " %</div>"
                } else {
                    result += textField(params) + " %"
                }
                break
            case AnswerType.range:
                // calculate range labels - sample adata = {interval: 4, start: 1, end: 'over 54 years', over: 54, unit: 'years'}
                def items = []
                for (int i = q.adata.start; i < q.adata.end; i += q.adata.interval) {
                    def value = "${i}-${i + q.adata.interval - 1}"
                    def fromValue = String.format('%,d', i)
                    def toValue = String.format('%,d', i + q.adata.interval - 1)
                    /* HACK HERE til after demo*/
                    def decoratedRange
                    if (q.adata.unitPlacement == 'beforeEach') {
                        decoratedRange = "${decorateValue(fromValue, q.adata.unit, q.adata.unitPlacement)} - " +
                                "${decorateValue(toValue, q.adata.unit, q.adata.unitPlacement)}"
                    }
                    else {
                        decoratedRange = "${fromValue} - " +
                                "${decorateValue(toValue, q.adata.unit, q.adata.unitPlacement)}"
                    }
                    items << [label: decoratedRange, value: value]
                }
                if (q.adata.over) {
                    def value = String.format('%,d', q.adata.end + 1)
                    def decoratedRange = decorateValue(value, q.adata.unit, q.adata.unitPlacement) + " " + q.adata.over
                    items << [label: decoratedRange, value:"${q.adata.end + 1}-"]
                }
                if (q.adata.alt) {
                    items << [label:q.adata.alt, value:q.adata.alt]
                }

                def widgets = []
                items.eachWithIndex { it, idx ->
                    def checked = (it.value == q.answerValueStr) ? "checked" : ""
                    def id = "${q.ident()}_${idx}"
                    widgets << "<input class='radio' type='radio' name='${q.ident()}' id='${id}' ${checked} value='${it.value}'/>"+
                            "<label for='${id}'>${it.label}</label><br/>"
                }

                // if there are lots of widgets we want to put them in two columns
                result += layoutListOfItems(widgets, 7)

                break
            case AnswerType.rank:
                result += textField(name: q.ident(), size: 4, value: q.answerValueStr) + " " + (q.alabel ?: "")
                break
            case AnswerType.externalRef:
                if (q.adata =~ 'state') {
                    if (q.displayHint == 'combobox') {
                        result += '<div class="ui-widget">' + combobox(name: q.ident(), size: 25, value: q.answerValueStr, mode: 'exclusive')
                    }
                    result += select(name: q.ident(), from: ListLoaderService.states, value: q.answerValueStr,
                            noSelection: ['':'Select a state or territory'])
                    result += '</div>'
                }
                else if (q.adata =~ 'herbarium') {
                    if (q.displayHint == 'combobox') {
                        result += '<div class="ui-widget">' + combobox(name: q.ident(), size: 38, value: q.answerValueStr)
                    }
                    result += select(name: q.ident(), from: ListLoaderService.herbaria, value: q.answerValueStr,
                                        noSelection: ['':'Select a herbarium'])
                    if (q.displayHint == 'combobox') {
                        result += '</div>'
                    }
                }
                else if (q.adata =~ 'museum') {
                    if (q.displayHint == 'combobox') {
                        result += '<div class="ui-widget">' + combobox(name: q.ident(), size: 38, value: q.answerValueStr)
                    }
                    result += select(name: q.ident(), from: ListLoaderService.museums, value: q.answerValueStr,
                                        noSelection: ['':'Select a museum'])
                    if (q.displayHint == 'combobox') {
                        result += '</div>'
                    }
                }
                else if (q.adata =~ 'university') {
                    if (q.displayHint == 'combobox') {
                        result += '<div class="ui-widget">' + combobox(name: q.ident(), size: 38, value: q.answerValueStr)
                    }
                    result += select(name: q.ident(), from: ListLoaderService.universities, value: q.answerValueStr,
                                        noSelection: ['':'Select a university'])
                    if (q.displayHint == 'combobox') {
                        result += '</div>'
                    }
                }
                else if (q.adata.domain) {
                    /* this uses a list from a database field based on the answer data elements
                      <domain>specifies the grails domain (table)</domain>
                      <property>specifies which column to use</property>
                      <questionSetProperty>indicates which column holds the question set id</questionSetProperty>
                      <noSelectionText>optionally specifies the text to show for no selection</noSelectionText>
                     */
                    // create and instantiate the domain class
                    def domainArtefact = grailsApplication.getDomainClass("au.org.ala.workforce.${q.adata.domain}")
                    def dom = domainArtefact.clazz.newInstance()

                    // get the set id column name
                    def qsid = q.adata.questionSetProperty

                    // find all domain instances for the current question set
                    def objs = dom.findAll("from ${q.adata.domain} as d where d.${qsid} = :set", [set:q.qset])

                    // extract the specified column values
                    def list = objs.collect {
                        it."${q.adata.property}"
                    }

                    // create the html select element
                    def noSelText = q.adata.noSelectionText ?: 'Select one'
                    result += select(name: q.ident(), from: list, value: q.answerValueStr,
                            noSelection: ['':noSelText])
                }
                break
            case AnswerType.preload:
                /* this uses answer metadata to specify the database record and column that will provide the answer value
                  <domain>specifies the grails domain (table)</domain>
                  <property>specifies which column to get the answer from</property>
                  <questionSetProperty>indicates which column holds the question set id</questionSetProperty>
                  <match-property>specifies the column to use to match the row</match-property>
                  <match>specifies the value to use to match the row (username is assumed for now)</match>
                 */

                // create and instantiate the domain class
                def domainArtefact = grailsApplication.getDomainClass("au.org.ala.workforce.${q.adata.domain}")
                def dom = domainArtefact.clazz.newInstance()

                // get the set id column name
                def qsid = q.adata.questionSetProperty

                // select the row
                def strToMatch = username()
                def matchColumn = q.adata.matchProperty

                // find all domain instances for the current question set
                //println "from ${q.adata.domain} as d where d.${qsid} = ${q.qset} and d.${matchColumn} = ${strToMatch}"
                def obj = dom.find("from ${q.adata.domain} as d where d.${qsid} = :set and d.${matchColumn} = :match",
                        [set:q.qset, match: strToMatch])
                assert obj

                // get the value to preload
                def answer = obj."${q.adata.property}"
                assert answer

                result += hiddenField(name: q.ident(), value: answer) + answer + " " + (q.alabel ?: "")
                break
            case AnswerType.calculate:
                def value = q.answerValueStr ?: '0'
                if (q.owner.qtype == QuestionType.matrix) {
                    def colNum = q.ident().substring(q.ident().lastIndexOf('_') + 1)
                    result += hiddenField(name: q.ident(), value: value, totalnum: colNum) + "<span class='calculated' id='s${q.ident()}' totalnum='${colNum}'>${value}</span>"
                } else {
                    def total = hiddenField(name: q.ident(), value: value, class: 'sumTotal') + "<span class='calculated' id='s${q.ident()}'>${value}</span> " + (q.alabel ?: "")
                    if (q.level == 2 && q.layoutHint == 'align-with-level3') {
                        result += "<div class='alignWithLevel3'>" + total + "</div>"
                    } else {
                        result += total
                    }
                    result += "<script type='text/javascript' src='/workforce/js/field-sum.js'></script>"
                }
                break
        }

        if (q.errorMessage) {
            def style = ''
            if (q.level == 2 && q.atype != AnswerType.none && q.atype != AnswerType.radio && q.atype != AnswerType.textarea) {
                style = "style='height:24px'"
            }
            result = "<div class='errors' ${style}>" + result + "</div>"
        }

        return result
    }

    Map<String, Integer> getRowAndColumnNumber(String ident, int noOfCols) {
        int field = ident.substring(ident.lastIndexOf('_') + 1) as int
        int colNumber = field.mod(noOfCols)
        if (colNumber == 0) {
            colNumber = noOfCols
        }
        int rowNumber = (field - 1) / noOfCols + 1
        return [rowNum: rowNumber, colNum: colNumber]
    }

    def combobox = {attrs ->
        def value = attrs.value ?: ''
        def options = "{value: '${value}'" + (attrs.size ? ", size: ${attrs.size}" : '') + (attrs.mode ? ", mode: '${attrs.mode}'" : '') + '}'
        return """
        <script type='text/javascript' src='/workforce/js/jquery-ui-combobox.js'></script>
        <script type='text/javascript'>
            \$(function() {
                \$('#${attrs.name}').combobox(${options});
            });
        </script>
"""
    }

    private Map getDisabled(QuestionModel q) {
        boolean enabled = false
        if (q.requiredIf) {
            def rif = q.parseCondition(q.requiredIf)
            switch (rif.comparator) {
                case "=":
                    if (q.getQuestionFromPath(rif.path)?.answerValueStr == rif.value) {
                        enabled = true
                    }
                    break
                case "=~":
                    if (q.getQuestionFromPath(rif.path)?.answerValueStr?.toLowerCase() =~ rif.value?.toLowerCase()) {
                        enabled = true
                    }
                    break
                case "groovyTruth":
                    if (q.getQuestionFromPath(rif.path)?.answerValueStr) {
                        enabled = true
                    }
                    break
            }

        }
        return enabled ? [:] : [disabled:'true']
    }

    /**
     * Decorates value with the unit if defined. unitPlacement affects whether the unit is placed
     * before of after the value.
     *
     * @param value to decorate
     * @param unit to add
     * @param unitPlacement where to add the unit
     * @return decorated value
     */
    private String decorateValue(value, unit, unitPlacement) {
        def result = value
        if (unit) {
            result = unitPlacement == 'beforeEach' ? unit + value : value + ' ' + unit
        }
        return result
    }

    private String layoutAnswer(QuestionModel q) {
        if (q.answerValueStr) {
            String result = "<span class='answer'>"
            
            switch (q.atype) {
                case AnswerType.bool:
                    if (q.answerValueStr?.toLowerCase() in ['yes', 'true', 'on']) {
                        result += "Yes"
                    } else if (q.answerValueStr?.toLowerCase() in ['no', 'false']) {
                        result += "No"
                    }
                    break

                case AnswerType.percent:
                    result += q.answerValueStr + "%"
                    break

                case AnswerType.range:
                    if (q.answerValueStr.endsWith("-")) {
                        result += "${q.answerValueStr.replace('-', '')} and over"
                    } else {
                        result += q.answerValueStr
                    }
                    break

                case AnswerType.textarea:
                    q.answerValueStr.tokenize("\r").each {
                        result += "${it}<br/>"
                    }
                    break
                
                default:
                    result += q.answerValueStr
            }

            return result + "</span>"
        } else {
            return ""
        }
    }

    /*
     * A matrix question is really a set of (rows x cols) questions. These questions are
     * generated during model-loading and numbered as sub-questions like this:
     *
     *              col 1       col2
     *   row 1        1          4
     *   row 2        2          5
     *   row 3        3          6
     *
     * All answers have the same type given by q.atype. (This may be enhanced in the future.)
     *
     * Assumes for now the matrix question is level 2, and the generated cell questions are level 3.
     */
    private Map layoutMatrix(QuestionModel q) {
        def text = q.qtext ?: ""

        def rows = q.qdata.rows
        def cols = q.qdata.cols
        int questionIdx = 0

        String content = "<table class='shy'>"

        content += "<col align='left' width='25%'/>"
        def colWidth = 75/cols.size()
        cols.each { content += "<col width='${colWidth}%'/>"}

        if (q.displayHint != 'noColumnHeadings') {
            // layout column headings
            content += "<tr><td>${text}</td>"

            cols.each {
                content += "<td style='text-align:center'>${it}</td>"
            }

            content += "</tr>"
        }

        // layout each row
        rows.each { row ->
            content += "<tr><td>${row}</td>"
            cols.each { col ->
                def qm = q.questions[questionIdx]
                if (qm) {
                    content += "<td style='text-align:center'>" + layoutWidget(qm) + "</td>"
                }
                else {
                    println "question not linked (${row}/${col})"
                }
                questionIdx++
            }
            content += "</tr>"
        }
        content += "</table>"

        if (q.atype == AnswerType.calculate || q.atype == AnswerType.summable) {
            content += "<script type='text/javascript' src='/workforce/js/column-sum.js'></script>"
        }

        return [secondColumnHtml: content, thirdColumnHtml: "", spanColumns2and3: true]
    }

    private Map layoutMatrixForReport(QuestionModel q, String userId) {
        def text = getQuestionTextForReport(q, userId) ?: ""

        def rows = q.qdata.rows
        def cols = q.qdata.cols
        int questionIdx = 0

        String content = "<table class='shy'>"

        content += "<col align='left' width='25%'/>"
        def colWidth = 75/cols.size()
        cols.each { content += "<col width='${colWidth}%'/>"}

        if (q.displayHint != 'noColumnHeadings') {
            content += "<tr><td>${text}</td>"

            cols.each {
                content += "<td style='text-align:center'>${it}</td>"
            }

            content += "</tr>"
        }

        rows.each { row ->
            // Check if there are any answers for this row
            def areAnswers = false
            q.questions[questionIdx .. questionIdx + cols.size() - 1].each {
                if (it.answerValueStr) {
                    areAnswers = true
                }
            }

            if (areAnswers) {
                content += "<tr><td>${row}</td>"
                cols.each { col ->
                    def qm = q.questions[questionIdx]
                    if (qm) {
                        content += "<td style='text-align:center'>" + layoutAnswer(qm) + "</td>"
                    }
                    else {
                        println "question not linked (${row}/${col})"
                    }
                    questionIdx++
                }
                content += "</tr>"
            } else {
                questionIdx += cols.size()
            }
        }
        content += "</table>"

        return [secondColumnHtml: content, thirdColumnHtml: "", spanColumns2and3: true, question: q]

    }
    /*
     * A radio matrix question is really just a list of questions. They are treated as a matrix so
     * that the radio buttons can be labelled as a group (in the column header)
     *
     *            option 1    option2
     *   row 1        o          o
     *   row 2        o          o
     *   row 3        o          o
     *
     * Each row is a separate question.
     * All answers have the type radio and the dataType of text (the value of the option).
     *
     * Assumes level 2 question for now.
     */
    private Map layoutRadioMatrix(QuestionModel q) {
        def text = q.qtext ?: ""

        def cols = q.qdata.cols

        String content = "<table class='shy'>"

        content += "<col align='left'/>"
        cols.each { content += "<col align='center'/>"}

        content += "<tr><td>${text}</td>"

        cols.each {
            content += "<td style='text-align:center'>${it}</td>"
        }

        content += "</tr>"

        q.questions.each { q2 ->
            def rowText = buildText(q2)
            if (q2.errorMessage) {
                rowText = "<span class='errors'>" + rowText + "</span>"
            }
            def ident = q2.ident()

            // optional radio groups should have a 'clear' link to reset the state
            def clearLink = ''
            if (rowText.toLowerCase() =~ 'other') {
                clearLink = " <span class='link' onclick=\"clearRadio('${ident}');\">clear answer</span>"
            }
            content += "<tr><td>${rowText}${clearLink}</td>"

            cols.eachWithIndex { col, idx ->
                def selected = (col == q2.answerValueStr) ? " checked" : ""
                def widget = "<input class='radio' type='radio'${selected} name='${ident}' id='${ident}_${idx}' value='${col}'/>" //TODO: handle error markup
                content += "<td style='text-align:center' width='15%'>" + widget + "</td>"
            }
            content += "</tr>"
        }
        content += "</table>"

        return [secondColumnHtml: content, thirdColumnHtml: "", spanColumns2and3: true]
    }

    private Map layoutRadioMatrixForReport(QuestionModel q) {
        
        String labels = "<table class='shy'>"
        String answers = "<table class='shy'>"

        q.questions.each {it ->
            if (it.answerValueStr) {
                labels += "<tr><td>${it.qtext}</td></tr>"
                answers += "<tr><td>${layoutAnswer(it)}</td></tr>"
            }
        }

        labels += "</table>"
        answers += "</table>"

        return [secondColumnHtml: labels, thirdColumnHtml: answers, spanColumns2and3: false, question: q]
    }

    private Map layoutGroup(QuestionModel q) {
        def items = []
        def currentSubgroup = ''
        def indent = ''
        q.questions.each {
            // handle sub-groups within the group
            if (it.layoutHint?.startsWith('subgroup:')) {
                def subgroup = it.layoutHint[9..-1]
                if (currentSubgroup != subgroup) {
                    indent = '&nbsp;&nbsp;&nbsp;'
                    items << indent + subgroup
                    currentSubgroup = subgroup
                }
            } else {
                currentSubgroup = ''
                indent = ''
            }
            items << indent + layoutWidget(it) + " ${buildText(it)}<br/>"
        }

        return [secondColumnHtml: layoutListOfItems(items, 6), thirdColumnHtml: "", spanColumns2and3: true]
    }

    private Map layoutGroupForReport(QuestionModel q, String userId) {
        def items = []
        q.questions.each {
            if (it.answerValueStr) {
                items << "<span class='answer'>${getQuestionTextForReport(it, userId)}</span>"
            }
        }

        return [secondColumnHtml: "", thirdColumnHtml: layoutListOfItemsForReport(items), spanColumns2and3: false, question: q]
    }

    private Map layoutRank(QuestionModel q) {
        def choices = "<table class='shy'>"
        q.questions.each {
            choices += "<tr><td style='text-align:center;padding-left:40px;' width='15%'>${layoutWidget(it)}</td><td width='85%'>${buildText(it)}</td></tr>"
        }
        choices += "</table>"

        return [secondColumnHtml: choices, thirdColumnHtml: "", spanColumns2and3: true]
    }

    private Map layoutRankForReport(QuestionModel q) {
        String labels = "<table class='shy'>"
        String answers = "<table class='shy'>"

        q.questions.each {it ->
            if (it.answerValueStr) {
                labels += "<tr><td>${it.qtext}</td></tr>"
                answers += "<tr><td>${layoutAnswer(it)}</td></tr>"
            }
        }

        labels += "</table>"
        answers += "</table>"

        return [secondColumnHtml: labels, thirdColumnHtml: answers, spanColumns2and3: false, question: q]
    }

    private String layoutLevel3(QuestionModel q) {
        String result = ''
        // layout 3rd level questions
        if (q.questions) {
            // determine layout params
            def layoutParams = extractLayoutHints(q.layoutHint)
            // determine displayHints
            def style = q.displayHint?.startsWith('style=') ? " " + q.displayHint : ""
            // create inner table to layout questions and answers
            result = "<table class='shy'${style}><colgroup><col width='${layoutParams.textWidth}%'/><col width='${layoutParams.widgetWidth}%'/></colgroup>"
            q.questions.each {
                def text = buildText(it) ?: ""
                def label = makeLabel(it)
                result += "<tr><td>${label} ${text}</td><td>" + layoutWidget(it) + "</td></tr>"
            }
            result += "</table>"
        }
        return result
    }

    private String layoutLevel3ForReport(QuestionModel q, String userId) {
         String result = ''
         // layout 3rd level questions
         if (q.questions) {
             String content = ''
             q.questions.each {
                 if (it.answerValueStr) {
                     def text = getQuestionTextForReport(it, userId) ?: ""
                     def label = makeLabel(it)
                     if (label) {
                         label += " "
                     }
                     content += "<tr><td>${label}${text}</td><td>" + layoutAnswer(it) + "</td></tr>"
                 }
             }
             if (content) {
                 // determine layout params
                 def layoutParams = extractLayoutHints(q.layoutHint)
                 // create inner table to layout questions and answers
                 result = "<table class='shy'><colgroup><col width='${layoutParams.textWidth}%'/><col width='${layoutParams.widgetWidth}%'/></colgroup>"
                 result += content + "</table>"
             }
         }
         return result
     }

    private int extractTextFieldSize(hint) {
        def result = 40
        if (hint) {
            try {
                result = Integer.parseInt(hint)
            } catch (NumberFormatException e) {
                println ">> warning: failed to parse text field size"
            }
        }
        return result
    }

    private Map extractLayoutHints(hint) {

        def result = [textWidth: 70, widgetWidth: 30]  // defaults
        if (hint) {

            // expect hint of the form "<textWidth>/<widgetWidth>" eg "30/70"
            def hints = hint.tokenize('/')

            if (hints.size() == 2) {
                try {
                    def tw = Integer.parseInt(hints[0])
                    def ww = Integer.parseInt(hints[1])
                    return [textWidth: tw, widgetWidth: ww]
                } catch (NumberFormatException e) {
                    println ">> warning: failed to parse layout hint"
                }
            }
        }
        return result
    }

    private String layoutListOfItems(List items, int wrapPoint) {
        //println "layoutListOfItems: items=${items?.size()} wrapPpoint=${wrapPoint}"
        if (items.size() < wrapPoint) {
            def result = "<table class='shy'>"
            items.each {
                result += "<tr><td>${it}</td></tr>"
            }
            result += "</table>"
            return result
        }

        if (items.size() % 2) {
            // odd number so add a blank
            items << ""
        }
        int half = items.size() / 2
        def result = "<table class='shy'>"
        for (int i = 0; i < half; i++) {
            result += "<tr><td width='50%'>${items[i]}</td><td width='50%'>${items[half + i]}</td></tr>"
        }
        result += "</table>"
        return result
    }

    private String layoutListOfItemsForReport(List items) {
         def result = "<table class='shy'>"
         for (int i = 0; i < items.size(); i++) {
             result += "<tr><td>${items[i]}</td></tr>"
         }
         result += "</table>"
         return result
    }

    static lowerAlpha = 'a'..'z'
    static lowerRoman = ['i','ii','iii','iv','v','vi','vii','viii','ix','x','xi','xii','xiii','xiv','xv','xvi','xvii','xviii','xix','xx']
    
    private String makeLabel(QuestionModel q) {
        if (!q.label) return ""

        def result = ""
        def prefix = ""
        def postfix = ""
        String pattern = q.label
        if (pattern[0] in ['(', '[']) {
            prefix = pattern[0]
            pattern = pattern - prefix
        }
        if (pattern[pattern.size()-1] in [')', ']']) {
            postfix = pattern[pattern.size()-1]
            pattern = pattern - postfix
        }
        if (pattern) {
            //println "q = ${q.questionNumber} pattern = ${pattern}"
            switch (pattern) {
                case "a":
                    // convert question no to lower case alpha
                    result = lowerAlpha[q.questionNumber - 1]
                    break
                case "1":
                    result = q.questionNumber
                    break
                case "i":
                    result = lowerRoman[q.questionNumber - 1]
                    break
                default:
                    result = ""
            }
        }
        return prefix + result + postfix
    }

    /*
     * Returns the number of rows that the first cell should span.
     *
     * If the 2nd level questions have labels, then this is only 1,
     * otherwise it is the number of rows required to display the whole question.
     */
    private int cell1RowSpan(q) {
        if (q.questions?.any {it.label}) {
            return 1
        } else {
            return q.calculateDisplayRows()
        }
    }

    def listError = {attrs ->
        if (attrs.error) {
            def levels = QuestionModel.parseIdent(attrs.error.key as String)
            if (levels) {
                out << "Q${levels[0]}"
                if (levels[1]) {
                    out << " - section ${levels[1]}"
                }
                if (levels[2]) {
                    out << " - part ${levels[2]}"
                }
                out << ": ${attrs.error.value}"
            }
        }
    }

    /**
     * Draws a page progress track.
     *
     * @total number of pages in set
     * @page one-based number of the page this is being drawn on
     * @set the question set to use in links
     */
    def pageProgress = {attrs->
        def total = attrs.total
        def pageNumber = attrs.page
        if (pageNumber && total && total > 2) {

            def track = pageNumber == 1 ?
                actionSubmit(action:'jumpPage', value: 1, class: 'first-step-on') :
                actionSubmit(action:'jumpPage', value: 1, class: 'first-step-off')

            2.upto(total-1) { num ->
                track += pageNumber == num ?
                    actionSubmit(action:'jumpPage', value:num, class: 'step-on') :
                    actionSubmit(action:'jumpPage', value:num, class: 'step-off')
                    //"<a href='${resource(file:'/set/'+ params.set +'/page/' + num)}'><img src='${resource(dir:'/images/abrsskin/', file: 'step-off.png')}'/></a>"
            }

            track += pageNumber == total ?
                actionSubmit(action:'jumpPage', value:total, class: 'last-step-on') :
                actionSubmit(action:'jumpPage', value:total, class: 'last-step-off')

            out << track
        }
    }

    def loggedInName = {
        out << username()
    }

    def isLoggedIn = { attrs, body ->
        if (AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) {
            out << body()
        }
    }

    def isNotLoggedIn = { attrs, body ->
        if (!AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) {
            out << body()
        }
    }

    def isABRSAdmin = { attrs, body ->
        if  (isAdmin()) {
            out << body()
        }
    }

    def isNotABRSAdmin = { attrs, body ->
        if  (!isAdmin()) {
            out << body()
        }
    }

    private String username() {
        if (ConfigurationHolder.config.security.cas.bypass) {
            'cas bypassed'
        }
        else if (request.getUserPrincipal()) {
            request.getUserPrincipal().getName()
        }
        else if (AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) {
            AuthenticationCookieUtils.getUserName(request)
        } else {
            ""
        }
    }

    private boolean isAdmin() {
        (ConfigurationHolder.config.security.cas.bypass || request?.isUserInRole("ROLE_ABRS_ADMIN"))
    }

    def reportNavigation = { attrs ->
        User user = attrs.user
        def users = attrs.users
        def userIndex = users.findIndexOf{ it == user }
        if (userIndex == -1) {
            userIndex = 0
        }
        def result

        result = "<div style='float: right; padding: 25px 0px; margin-right: 0; *margin-right: 50%'>"
        if (userIndex > 0) {
            result += "<span><a href='${resource(file:'/report/'+ params.set +'/user/' + users[userIndex-1].userid)}'>prev</a></span>"
        }
        if (userIndex < users.size() - 1) {
            result += "<span style='padding-left: 20px'><a href='${resource(file:'/report/'+ params.set +'/user/' + users[userIndex+1].userid)}'>next</a></span>"
        }
        result += "</div>"

        out << result
    }

    def selectSurvey = {
        if (username()) {
            def institutions = Institution.listInstitutionsForSet(Survey.getCurrentQSetId(SurveyType.institutional))
            if (institutions.any {it.account == username()}) {
                // show institutional survey
                /*out << link(controller:"question", action:"page", params:['set':2, 'page':1]) {
                        "<img src='${resource(dir:'images/abrsskin',file:'collections-button.png')}'/>" }*/
                out << 'institution'
            }
            else {
                // show personal survey
                /*out << link(controller:"question", action:"page", params:['set':1,'page':1]) {
                        "<img src='${resource(dir:'images/abrsskin',file:'personal-button.png')}'/>" }*/
                out << 'personal'
            }
        }
        else {
            out << 'login'
        }
    }

    def surveyStatus = { attrs ->
        int userid = ((AttributePrincipal) request.userPrincipal).attributes['userid'] as int
        def currentYear = ConfigData.getSurveyYear()
        def lastUpdate = Answer.lastUpdate(attrs.setid as int, userid, currentYear)
        if (lastUpdate) {
            lastUpdate = DateUtil.getNiceDateFromSqlDate(lastUpdate)
            def complete = Event.isComplete(attrs.setid as int, userid, currentYear)
            if (complete) {
                out << "<p>Survey complete - thank you. (Last modified ${lastUpdate})</p>"
            } else {
                out << "<p>Survey incomplete (Last modified ${lastUpdate})</p>"
            }
            out << "<p class='textLinks'><a href='"
            out << g.createLink(controller:"report", action:"answers", params:[set: attrs.setid, id:userid])
            out << "'>Click here to see a summary of your answers.</a></p>"
        }
    }

    def summaryStatus = { attrs ->
        int userid = attrs.user.userid
        def currentYear = ConfigData.getSurveyYear()
        def lastUpdate = DateUtil.getNiceDateFromSqlDate(Answer.lastUpdate(params.set as int, userid, currentYear))
        def complete = Event.isComplete(params.set as int, userid, currentYear)
        if (complete) {
            out << "Survey complete (Last modified ${lastUpdate})"
        } else {
            out << "Survey incomplete (Last modified ${lastUpdate})"
        }
    }

    private boolean areAnswers(QuestionModel q) {
        if (q.answerValueStr && !q.answerValueStr.equalsIgnoreCase('null')) {
            return true
        }
        for (it in q.questions) {
            if (it.answerValueStr && !it.answerValueStr.equalsIgnoreCase('null')) {
                return true
            }
        }
        return false
    }

    private boolean isAggregation(QuestionModel q) {
        if (q.aggregations) {
            return true
        }
        for (it in q.questions) {
            if (isAggregation(it)) {
                return true
            }
        }
        return false
    }

    private String getQuestionTextForReport(QuestionModel q, String userId) {
        def answer
        if (q.shorttext) {
            answer = q.shorttext
        } else {
            answer = q.qtext
        }

        if (q.atype == AnswerType.range) {
            if (q.adata.unit) {
                answer += " (${q.adata.unit})"
            }
        }

        if (isAggregation(q) && isAdmin()) {
            answer += getChartLink(q, userId)
        }

        return answer
    }

    /**
     * Combines text and optional subtext into an HTML string.
     * @return
     */
    private String buildText(QuestionModel q) {
        def text = q.qtext
        if (q.subtext) {
            text += "<br/><span class='subtext'>${q.subtext}</span>"
        }
        if (q.instruction && q.instructionPosition == 'top') {
            text += "<br/><span class='instruction'>${q.instruction}</span>"
        }
        return text
    }

    def setDisabledState(QuestionModel q) {
        def path = q.dependentOn
        if (!path) { return }

        // get the conditional phrase
        def cond = QuestionModel.parseCondition(q.dependentOn)

        // assume simple path for now
        int targetQuestionNumber = cond.path[1..-1].toInteger()

        // load the question
        def answers = Answer.getAnswers(q.qset, 21, ConfigData.getSurveyYear())
        QuestionModel contingent =  modelLoaderService.loadQuestionWithAnswer(q.qset, targetQuestionNumber, answers)

        // check the answer
        String answer = contingent.answerValueStr
        switch (cond.comparator) {
            case "=":
                if (answer != cond.value) {
                    println "setting ${q.ident()} to disabled"
                    q.disabled = true
                }
                break
            case "=~":
                if (!(answer?.toLowerCase() =~ cond.value?.toLowerCase())) {
                    q.disabled = true
                }
                break
            case "groovyTruth":
                if (!answer) {
                    q.disabled = true
                }
                break
        }
    }

    private String getQuestionLink(QuestionModel q, QuestionSet qset, String userId) {
        def page = qset.findPageByQuestionNumber(q.questionNumber)
        def loggedInUserId = request.userPrincipal.attributes.userid
        if (userId) {
            if (userId == loggedInUserId) {
                return "<a href='/workforce/set/${q.qset}/page/${page.pageNumber}#Q${q.questionNumber}' id='Q${q.questionNumber}'>Q${q.questionNumber}</a>"
            } else {
                return "<a id='Q${q.questionNumber}'>Q${q.questionNumber}</a>"
            }
        } else {
            return "<a id='Q${q.questionNumber}'>Q${q.questionNumber}</a>"
        }
    }

    private String getChartLink(QuestionModel q, String userId) {
        if (q.level == 1 || q.level == 2 && !q.owner.qtext && q.questionNumber == 1) {
            return " <a href='/workforce/report/charts?set=${q.qset}&qid=${q.questionNumber}&uid=${userId}'><img src='/workforce/images/chart.gif'></a>"
        } else {
            return ''
        }
    }

    private List getQuestionGuids(QuestionModel q, String subLevel) {
        List guids = []
        def tokens = subLevel.tokenize('/=')
        def level = tokens[0]
        def qtext
        def col
        def type
        def range = []
        if (tokens.size() > 1) {
            switch (tokens[1]) {
                case 'qtext':
                    qtext = tokens[2]
                    break
                case 'col':
                    col = tokens[2]
                    break
                case 'type':
                    type = tokens[2]
                    break
                case 'range':
                    def rangeLimits = tokens[2].tokenize('..')
                    for (int i = rangeLimits[0] as int; i <= (rangeLimits[1] as int); i++) {
                        range << i - 1
                    }
                    break
            }
        }

        def getGuids = { list, question ->
            if (qtext) {
                if (question.qtext == qtext) {
                    list << question.guid
                }
            } else if (col) {
                if (q.qdata.cols[(col as int) - 1] == question.adata.col) {
                    list << question.guid
                }
            } else if (type) {
                if (question.atype == AnswerType.valueOf(type)) {
                    list << question.guid
                }
            } else {
                list << question.guid
            }
        }

        switch (level) {
            case '1':
                q.questions.each { it ->
                    getGuids(guids, it)
                }
                break

            case '2':
                q.questions.eachWithIndex { it, i ->
                    if (range) {
                        if (i in range) {
                            it.questions.each { it2 ->
                                getGuids(guids, it2)
                            }
                        }
                    } else {
                        it.questions.each { it2 ->
                            getGuids(guids, it2)
                        }
                    }
                }
                break

            case 'all':
                q.questions.each { it ->
                    getGuids(guids, it)
                    it.questions.each { it2 ->
                        getGuids(guids, it2)
                    }
                }
                break
        }

        return guids
    }

    private Map getGroupByOptions(String groupBy) {
        Map options = [:]
        def tokens = groupBy.tokenize('/')
        options['type'] = tokens[0]
        options['range'] = tokens[1]
        options['unit'] = tokens[2]
        return options
    }

    private String percentage(int value, int total) {
        def percent = value/total * 100.0
        return String.format('%.1f', percent)
    }

    private int min(int a, int b) {
        if (a < b) {
            return a
        } else {
            return b
        }
    }
}