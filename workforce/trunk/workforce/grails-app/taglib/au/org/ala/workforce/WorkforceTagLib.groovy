package au.org.ala.workforce

import java.text.NumberFormat

class WorkforceTagLib {

    static namespace = 'wf'

    def modelLoaderService

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
                  out << "<td colspan=2>${model.qtext}</td>"
                out << "</tr>"

            } else {
                // go straight to first sub-question
                out << "<tr>"
                  out << "<td rowspan='${firstCellRowSpan}'>Q${model.questionNumber}</td>"
                if (secondLevel) {
                  QuestionModel q = secondLevel[secondLevelIndex++]
                  out << "<td>${q.qtext}</td>"
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
                  out << "<td>${model.qtext}</td>"
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
        if (model.instruction) {
            // add in bottom row
            out << "<tr>"
                out << "<td colspan=2><strong><em>${model.instruction}</em></strong></td>"
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

        else if (q.atype == AnswerType.rank) {
            result = layoutRank(q)
        }

        else if (q.questions) {
            // all level 2 in column 2; level 3 in column 3
            if (q.displayHint == 'checkbox') {
                // widget first
                result.secondColumnHtml = layoutWidget(q) + " ${q.qtext}"
            } else {
                // text first
                result.secondColumnHtml = "${q.qtext} " + layoutWidget(q)
            }
            // 3rd level questions
            result.thirdColumnHtml = layoutLevel3(q)

        }

        else {
            // no 3rd level questions - put widgets in 3rd column unless there is no content for the 2nd
            if (q.qtext) {
                result.secondColumnHtml = q.qtext
                result.thirdColumnHtml = layoutWidget(q)
            } else {
                result.secondColumnHtml = layoutWidget(q)
                result.spanColumns2and3 = true
            }
        }
        return result
    }

    static statesList = ['Select a state or territory', 'Australian Capital Territory', 'New South Wales', 'Queensland', 'Northern Territory', 'Western Australia', 'South Australia', 'Tasmania', 'Victoria']

    private String layoutWidget(QuestionModel q) {
        String result = ''

        switch (q.atype) {
            case AnswerType.bool:
                def yesChecked = (q.answerValueStr?.toLowerCase() in ['yes', 'true', 'on']) ? 'checked' : ''
                def noChecked = (q.answerValueStr?.toLowerCase() in ['no', 'false']) ? 'checked' : ''
                if (q.displayHint == 'checkbox') {
                    result += "<input type='checkbox' name='${q.ident()}' ${yesChecked}/>"
                } else {
                    result += "<input type='radio' name='${q.ident()}' id='yes' value='yes' ${yesChecked}/><label for='yes'>Yes</label>"
                    result += "<input type='radio' name='${q.ident()}' id='no' value='no' ${noChecked}/><label for='no'>No</label>"
                }
                break
            case AnswerType.none:
                break
            case AnswerType.number:
                result += textField(name: q.ident(), size: 4, value: q.answerValueStr) + " " + (q.alabel ?: "")
                break
            case AnswerType.radio:
                // display radio buttons with text from adata to select one chunk of text
                def items = []
                q.adata.eachWithIndex { it, idx ->
                    def checked = it == q.answerValueStr ? 'checked' : ''
                    items << "<input type='radio' name='${q.ident()}' id='${q.ident()}_${idx}' value='${it}' ${checked}/><label for='${q.ident()}_${idx}'>${it}</label>"
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
                result += textArea(name: q.ident(), rows: q.adata?.rows, value: q.answerValueStr) + " " + (q.alabel ?: "")
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
                    if (q.adata.unit && q.adata.unitPlacement) {
                        switch (q.adata.unitPlacement) {
                            case 'beforeEach':
                                items << [label:"${q.adata.unit}${fromValue} - ${q.adata.unit}${toValue}",
                                           value:value]
                                break
                            default:
                                items << [label:"${fromValue} - ${toValue} ${q.adata.unit}", value:value]
                                break
                        }
                    } else {
                        items << [label:"${fromValue} - ${toValue} ${q.adata.unit}", value:value]
                    }
                }
                if (q.adata.over) {
                    items << [label:q.adata.over, value:"over"]
                }
                if (q.adata.alt) {
                    items << [label:q.adata.alt, value:"alt"]
                }

                def widgets = []
                items.eachWithIndex { it, idx ->
                    def checked = (it.value == q.answerValueStr) ? "checked" : ""
                    def id = "${q.ident()}_${idx}"
                    widgets << "<input type='radio' name='${q.ident()}' id='${id}' ${checked} value='${it.value}'/>"+
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
                    result += select(name: q.ident(), from: statesList, value: q.answerValueStr)
                }
                break
        }

        if (q.errorMessage) {
            result = "<div class='errors'>" + result + "</div>"
        }

        return result
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

        def rows = q.qdata.rows
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
            def rowText = q2.qtext
            if (q2.errorMessage) {
                rowText = "<span class='errors'>" + rowText + "</span>"
            }
            content += "<tr><td>${rowText}</td>"

            def ident = q2.ident()

            cols.eachWithIndex { col, idx ->
                def selected = (col == q2.answerValueStr) ? " checked" : ""
                def widget = "<input type='radio'${selected} name='${ident}' id='${ident}_${idx}' value='${col}'/>" //TODO: handle error markup
                content += "<td style='text-align:center' width='15%'>" + widget + "</td>"
            }
            content += "</tr>"
        }
        content += "</table>"

        return [secondColumnHtml: content, thirdColumnHtml: "", spanColumns2and3: true]
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
            items << indent + layoutWidget(it) + " ${it.qtext}<br/>"
        }

        return [secondColumnHtml: layoutListOfItems(items, 6), thirdColumnHtml: "", spanColumns2and3: true]
    }

    private Map layoutRank(QuestionModel q) {
        def choices = "<table class='shy'>"
        q.questions.each {
            choices += "<tr><td style='text-align:center;padding-left:40px;' width='15%'>${layoutWidget(it)}</td><td width='85%'>${it.qtext}</td></tr>"
        }
        choices += "</table>"

        return [secondColumnHtml: choices, thirdColumnHtml: "", spanColumns2and3: true]
    }

    private String layoutLevel3(QuestionModel q) {
        String result = ''
        // layout 3rd level questions
        if (q.questions) {
            // determine layout params
            def layoutParams = extractLayoutHints(q.layoutHint)
            // create inner table to layout questions and answers
            result = "<table class='shy'><colgroup><col width='${layoutParams.textWidth}%'/><col width='${layoutParams.widgetWidth}%'/></colgroup>"
            q.questions.each {
                def text = it.qtext ?: ""
                def label = makeLabel(it)
                result += "<tr><td>${label} ${text}</td><td>" + layoutWidget(it) + "</td></tr>"
            }
            result += "</table>"
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

}