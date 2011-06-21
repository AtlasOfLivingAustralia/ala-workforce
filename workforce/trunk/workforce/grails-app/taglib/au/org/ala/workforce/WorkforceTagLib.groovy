package au.org.ala.workforce

import au.org.ala.cas.util.AuthenticationCookieUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import java.text.NumberFormat
import java.text.DecimalFormat

/**
 * Notes:
 *
 * radio buttons must have class='radio' to style background color and border as IE6 doesn't handle attribute selectors
 * checkboxes must have class='checkbox' to style background color and border as IE6 doesn't handle attribute selectors
 */

class WorkforceTagLib {

    static namespace = 'wf'

    static final int INSTITUTION_SURVEY_QUESTION_SET_ID = 2;

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
                  out << "<td rowspan='${firstCellRowSpan}'>Q${model.questionNumber}</td>"
                  out << "<td colspan=2>${buildText(model)}</td>"
                out << "</tr>"

            } else {
                // go straight to first sub-question
                out << "<tr>"
                  out << "<td rowspan='${firstCellRowSpan}'>Q${model.questionNumber}</td>"
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
                  out << "<td rowspan='${firstCellRowSpan}'>Q${model.questionNumber}</td>"
                  out << "<td>${buildText(model)}</td>"
                  out << "<td>" + layoutWidget(model) + "</td>"
                out << "</tr>"

            } else {
                // probably won't occur
                out << "<tr>"
                  out << "<td rowspan='${firstCellRowSpan}'>Q${model.questionNumber}</td>"
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
                contents << generateContentForReport(q)
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
                    out << "<td rowspan='${firstCellRowSpan}'>Q${model.questionNumber}</td>"
                    if (firstCellRowSpan > 1) {
                        out << "<td colspan='2'>${getQuestionTextForReport(model)}</td>"
                    } else {
                        out << "<td>${getQuestionTextForReport(model)}</td><td/>"
                    }
                out << "</tr>"

            } else {
                // go straight to first sub-question
                out << "<tr>"
                    out << "<td rowspan='${firstCellRowSpan}'>Q${model.questionNumber}</td>"
                if (secondLevel) {
                    QuestionModel q = secondLevel[0]
                    if (contents) {contents.remove(0)}
                    out << "<td>${getQuestionTextForReport(q)}</td>"
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
                    out << "<td rowspan='${firstCellRowSpan}'>Q${model.questionNumber}</td>"
                    out << "<td>${getQuestionTextForReport(model)}</td>"
                    out << "<td>" + layoutAnswer(model) + "</td>"
                out << "</tr>"

            } else {
                // probably won't occur
                out << "<tr>"
                    out << "<td rowspan='${firstCellRowSpan}'>Q${model.questionNumber}</td>"
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

    private Map generateContentForReport(QuestionModel q) {
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
            result = q.atype == AnswerType.radio ? layoutRadioMatrixForReport(q) : layoutMatrixForReport(q)
        }

        else if (q.qtype == QuestionType.group) {
            result = layoutGroupForReport(q)
        }

        else if (q.qtype == QuestionType.rank) {
            result = layoutRankForReport(q)
        }

        else if (q.questions) {
            // 3rd level questions
            result.thirdColumnHtml = layoutLevel3ForReport(q)

            if (result.thirdColumnHtml) {
                if (q.displayHint == 'checkbox') {
                     // widget first
                     result.secondColumnHtml = "${getQuestionTextForReport(q)}"
                 } else {
                     // text first
                     result.secondColumnHtml = "${getQuestionTextForReport(q)} " + layoutAnswer(q)
                 }
            }
        }

        else {
            // no 3rd level questions - put widgets in 3rd column unless there is no content for the 2nd
            if (q.qtext) {
                result.secondColumnHtml = getQuestionTextForReport(q)
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
                if (q.displayHint == 'checkbox') {
                    result += "<input class='checkbox' type='checkbox' name='${q.ident()}' ${yesChecked}/>"
                } else {
                    result += "<input class='radio' class='radio' type='radio' name='${q.ident()}' id='yes' value='yes' ${yesChecked}/><label for='yes'>Yes</label>"
                    result += "<input class='radio' type='radio' name='${q.ident()}' id='no' value='no' ${noChecked}/><label for='no'>No</label>"
                }
                break
            case AnswerType.none:
                break
            case AnswerType.number:
                def answer = q.answerValueStr?.isNumber() ? NumberFormat.getInstance().format(q.answerValueStr.toFloat()) : q.answerValueStr
                def atts = [name: q.ident(), size: 8, value: answer, 'class': 'number']
                if (q.onchangeAction) {
                    atts.put 'onchange', "${q.onchangeAction}();"
                }
                result += textField(atts) + " " + (q.alabel ?: "")
                break
            case AnswerType.radio:
                // display radio buttons with text from adata to select one chunk of text
                def items = []
                q.adata.eachWithIndex { it, idx ->
                    def checked = it == q.answerValueStr ? 'checked' : ''
                    items << "<input class='radio' type='radio' name='${q.ident()}' id='${q.ident()}_${idx}' value='${it}' ${checked}/><label for='${q.ident()}_${idx}'>${it}</label>"
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
                result += textField(name: q.ident(), size: size, value: q.answerValueStr) + " " + (q.alabel ?: "")
                break
            case AnswerType.textarea:
                result += textArea(name: q.ident(), rows: q.adata?.rows ?: 4, value: (q.answerValueStr ?: "")) + " " + (q.alabel ?: "")
                break
            case AnswerType.percent:
                def attrs = [name: q.ident(), size: 7, value: q.answerValueStr]
                if (q.level == 2 && q.layoutHint == 'align-with-level3') {  //TODO: apply to other types
                    result = "<div class='alignWithLevel3'>" + textField(attrs) + " %</div>"
                } else {
                    result += textField(attrs) + " %"
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
                    result += select(name: q.ident(), from: ListLoaderService.states, value: q.answerValueStr,
                            noSelection: ['':'Select a state or territory'])
                }
                else if (q.adata =~ 'university') {
                    result += select(name: q.ident(), from: ListLoaderService.universities, value: q.answerValueStr,
                            noSelection: ['':'Select a university'])
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
                // only support adata = 'sum' for now - which sums the number answers from siblings
                def sum = 0
                q.owner.questions.each {
                    if (it.atype == AnswerType.number && it.answerValueStr) {
                        if (it.answerValueStr.isNumber()) {
                            sum += NumberFormat.getInstance().parse(it.answerValueStr)
                        }
                    }
                }
                result += hiddenField(name: q.ident(), value: sum) + "<span class='calculated' id='s${q.ident()}'>${sum}</span> " + (q.alabel ?: "")
                def questionRoot = q.ident()[0..q.ident().lastIndexOf('_')]
                def start = 1
                def end = q.owner.questions.size() - 1
                def target = q.ident()
                result += """
<script>
  function ${q.adata.action}() {
    var questionRoot = '${questionRoot}';
    var start = ${start};
    var end = ${end};
    var target = '${target}';
    var sum = 0
    for (var i = start; i <= end; i++) {
      var num = parseFloat(\$('input#'+questionRoot+i).attr('value'));
      if (!isNaN(num)) {
        sum = sum + num
      }
    }
    \$('input#'+target).attr('value',sum);
    \$('span#s'+target).html(sum);
  }
</script>
"""
                break
        }

        if (q.errorMessage) {
            if (q.level == 3) {
                result = "<div class='errors'>" + result + "</div>"
            } else {
                result = "<div class='errors' style='height:24px'>" + result + "</div>"
            }
        }

        return result
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

        content += "<tr><td>${text}</td>"

        cols.each {
            content += "<td style='text-align:center'>${it}</td>"
        }

        content += "</tr>"

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

        return [secondColumnHtml: content, thirdColumnHtml: "", spanColumns2and3: true]
    }

    private Map layoutMatrixForReport(QuestionModel q) {
        def text = getQuestionTextForReport(q) ?: ""

        def rows = q.qdata.rows
        def cols = q.qdata.cols
        int questionIdx = 0

        String content = "<table class='shy'>"

        content += "<col align='left' width='25%'/>"
        def colWidth = 75/cols.size()
        cols.each { content += "<col width='${colWidth}%'/>"}

        content += "<tr><td>${text}</td>"

        cols.each {
            content += "<td style='text-align:center'>${it}</td>"
        }

        content += "</tr>"

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
            content += "<tr><td>${rowText}</td>"

            def ident = q2.ident()

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

    private Map layoutGroupForReport(QuestionModel q) {
        def items = []
        q.questions.each {
            if (it.answerValueStr) {
                items << "<span class='answer'>${getQuestionTextForReport(it)}</span>"
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

    private String layoutLevel3ForReport(QuestionModel q) {
         String result = ''
         // layout 3rd level questions
         if (q.questions) {
             String content = ''
             q.questions.each {
                 if (it.answerValueStr) {
                     def text = getQuestionTextForReport(it) ?: ""
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
        def users = User.list([sort:'name'])
        def userIndex = users.findIndexOf{ it == user }
        def result = ''

        if (userIndex != -1) {
            result = "<div style='float: right; padding: 25px 0px; margin-right: 0; *margin-right: 50%'>"
            if (userIndex > 0) {
                result += "<span><a href='${resource(file:'/report/'+ params.set +'/user/' + users[userIndex-1].userid)}'>prev</a></span>"
            }
            if (userIndex < users.size() - 1) {
                result += "<span style='padding-left: 20px'><a href='${resource(file:'/report/'+ params.set +'/user/' + users[userIndex+1].userid)}'>next</a></span>"
            }
            result += "</div>"
        }

        out << result
    }

    def selectSurvey = {
        if (username()) {
            def institutions = Institution.listInstitutionsForSet(INSTITUTION_SURVEY_QUESTION_SET_ID)
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

    private boolean areAnswers(QuestionModel q) {
        for (it in q.questions) {
            if (it.answerValueStr && !it.answerValueStr.equalsIgnoreCase('null')) {
                return true
            }
        }
        if (q.answerValueStr && !q.answerValueStr.equalsIgnoreCase('null')) {
            return true
        }
        return false
    }

    private String getQuestionTextForReport(QuestionModel q) {
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

}