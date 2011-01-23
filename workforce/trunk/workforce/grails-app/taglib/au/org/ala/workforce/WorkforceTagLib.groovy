package au.org.ala.workforce

class WorkforceTagLib {

    static namespace = 'wf'

    def question = { attrs ->
        QuestionModel model = attrs.question

        /*
         * The top level may:
         *  - just be a container for subquestions (no qtext and no answer)
         *  - just be the carrier for the question text (qtext but no answer)
         *  - be a real question (both qtext and answer)
         *  - (?) take an answer but have no question text (no qtext but answer)
         * This determines layout structure.
         */

        //println "atype = ${model.atype}(${model.atype?.class}), qtype = ${model.qtype}"

        List secondLevel = model.questions
        int secondLevelIndex = 0

        // pre-calculate whether the first cell needs to span all rows
        int firstCellRowSpan = cell1RowSpan(model)

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

    static statesList = ['Australian Capital Territory', 'New South Wales', 'Queensland', 'Northern Territory', 'Western Australia', 'South Australia', 'Tasmania', 'Victoria']

    private String layoutWidget(QuestionModel q) {
        String result = ''

        //println q.toString()
        // types = yesno, none, number, text, textarea, percent, rank, externalRef, radio, range

        switch (q.atype) {
            case AnswerType.yesno:
                if (q.displayHint == 'checkbox') {
                    result = "<input type='checkbox' name='${q.ident()}'/>"
                } else {
                    result = "<input type='radio' name='${q.ident()}' id='yes' value='yes'/><label for='yes'>Yes</label>"
                    result += "<input type='radio' name='${q.ident()}' id='no' value='no'/><label for='no'>No</label>"
                }
                break
            case AnswerType.none:
                break
            case AnswerType.number:
                result = textField(name: q.ident(), size: 4) + " " + (q.alabel ?: "")
                break
            case AnswerType.radio:
                // display radio buttons with text from adata to select one chunk of text
                def items = []
                q.adata.eachWithIndex { it, idx ->
                    //println "option ${idx} = ${it}"
                    items << "<input type='radio' name='${q.ident()}' id='${q.ident()}_${idx}' value='${it}'/><label for='${q.ident()}_${idx}'>${it}</label><br/>"
                }
                result = layoutListOfItems(items, 7)
                break
            case AnswerType.text:
                def size = extractTextFieldSize(q.displayHint)
                result = textField(name: q.ident(), size: size) + " " + (q.alabel ?: "")
                break
            case AnswerType.textarea:
                result = textArea(name: q.ident(), rows: q.adata?.rows) + " " + (q.alabel ?: "")
                break
            case AnswerType.percent:
                result = textField(name: q.ident(), size: 7) + " %"
                break
            case AnswerType.range:
                // calculate range labels - sample adata = {interval: 4, start: 1, end: 'over 54 years', over: 54, unit: 'years'}
                def labels = []
                for (int i = q.adata.start; i < q.adata.end; i += q.adata.interval) {
                    if (q.adata.unit && q.adata.unitPlacement) {
                        switch (q.adata.unitPlacement) {
                            case 'beforeEach':
                                labels << "${q.adata.unit}${i} - ${q.adata.unit}${i + q.adata.interval - 1}"
                                break
                            default:
                                labels << "${i} - ${i + q.adata.interval - 1} ${q.adata.unit}"
                                break
                        }
                    } else {
                        labels << "${i} - ${i + q.adata.interval - 1} ${q.adata.unit}"
                    }
                }
                if (q.adata.over) {
                    labels << q.adata.over
                }
                if (q.adata.alt) {
                    labels << q.adata.alt
                }

                def items = []
                labels.eachWithIndex { it, idx ->
                    items << "<input type='radio' name='${q.ident()}' id='${q.ident()}_${idx}' value='${it}'/><label for='${it}'>${it}</label><br/>"
                }

                // if there are lots of items we want to put them in two columns
                result = layoutListOfItems(items, 7)

                break
            case AnswerType.rank:
                break
            case AnswerType.externalRef:
                if (q.adata =~ 'state') {
                    result = select(name: q.ident(), from: statesList)
                }
                break
        }

        return result
    }

    /*
     * A matrix question is really a set of (rows x cols) questions. These questions are
     * generated and numbered as sub-questions like this:
     *
     *              col 1       col2
     *   row 1        1          4
     *   row 2        2          5
     *   row 3        3          6
     *
     * All answers have the same type given by q.atype. (This may be enhanced in the future.)
     *
     * Assumes level 2 question for now.
     */
    private Map layoutMatrix(QuestionModel q) {
        def text = q.qtext ?: ""

        def rows = q.qdata.rows
        def cols = q.qdata.cols
        def questionIdx = 1

        String content = "<table class='shy'>"

        content += "<col align='left'/>"
        cols.each { content += "<col align='center'/>"}

        content += "<tr><td>${text}</td>"

        cols.each {
            content += "<td style='text-align:center'>${it}</td>"
        }

        content += "</tr>"

        rows.each { row ->
            content += "<tr><td>${row}</td>"
            cols.each { col ->
                def record = [datatype: q.datatype, atype: q.atype, level1: q.level1(), level2: questionIdx++, level3: 0]
                QuestionModel qm = new QuestionModel(record)
                q.questions << qm
                qm.owner = q
                content += "<td style='text-align:center'>" + layoutWidget(qm) + "</td>"
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
        def questionIdx = 1

        String content = "<table class='shy'>"

        content += "<col align='left'/>"
        cols.each { content += "<col align='center'/>"}

        content += "<tr><td>${text}</td>"

        cols.each {
            content += "<td style='text-align:center'>${it}</td>"
        }

        content += "</tr>"

        rows.each { row ->
            content += "<tr><td>${row}</td>"

            // this logically creates questions that are implied by the matrix
            // the questions exist temporarily to encapsulate the numbering operations
            def record = [datatype: q.datatype, atype: q.atype, level1: q.level1(), level2: questionIdx++, level3: 0]
            QuestionModel qm = new QuestionModel(record)
            q.questions << qm
            qm.owner = q

            cols.eachWithIndex { col, idx ->
                def widget = "<input type='radio' name='${qm.ident()}' id='${qm.ident()}_${idx}' value='${col}'/>"
                content += "<td style='text-align:center' width='15%'>" + widget + "</td>"
            }
            content += "</tr>"
        }
        content += "</table>"

        return [secondColumnHtml: content, thirdColumnHtml: "", spanColumns2and3: true]
    }

    private Map layoutGroup(QuestionModel q) {
        def items = []
        q.questions.each {
            items << layoutWidget(it) + " ${it.qtext}<br/>"
        }

        return [secondColumnHtml: layoutListOfItems(items, 2), thirdColumnHtml: "", spanColumns2and3: true]
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
}