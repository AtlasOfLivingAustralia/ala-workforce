package au.org.ala.workforce

import groovy.sql.Sql
import grails.converters.JSON
import org.xml.sax.SAXException
import grails.converters.XML
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import groovy.xml.XmlUtil

class DataLoaderService implements ApplicationContextAware {

    static transactional = false
    javax.sql.DataSource dataSource
    ApplicationContext applicationContext

    /**
     * Clear existing questions in a set.
     *
     * @return
     */
    def clearQuestionSet(set) {
        // clear existing
        def sql = new Sql(dataSource)
        sql.execute("delete from question where qset = ${set}")
        sql.execute("delete from question_set where qset = ${set}")
        sql.execute("delete from institution where setId = ${set}")
    }

    /**
     * Clear all existing questions from database.
     *
     * Only used for testing.
     * @return
     */
    def clearQuestionSets() {
        // clear existing
        def sql = new Sql(dataSource)
        sql.execute("delete from question")
        sql.execute("delete from question_set")
        sql.execute("delete from institution")
    }

    /**
     * Load the specified question set.
     *
     * Looks for the GUID-annotated version first, then the raw version; external files first then internal.
     * @param set the integer identifier of the set
     */
    def loadQuestionSet(set) {
        // first try external file with guids
        def qsetFile = new File("/data/workforce/metadata/question-set-with-guids-${set}.xml")
        if (!qsetFile.exists()) {
            // second try external file without guids
            qsetFile = new File("/data/workforce/metadata/question-${set}.xml")
        }
        if (!qsetFile.exists()) {
            // third try internal file with guids
            qsetFile = applicationContext.getResource("metadata/question-set-with-guids-${set}.xml").getFile()
        }
        if (!qsetFile.exists()) {
            // fourth try internal file without guids
            qsetFile = applicationContext.getResource("metadata/question-set-${set}.xml").getFile()
        }
        assert qsetFile : "question set definition not found"

        // inject any missing guids
        def file = injectGuidsAndSave(set, qsetFile)
        assert file : "unable to write question set file with guids"

        loadQuestionSetXML(file.text)
    }

    /**
     * Add any missing guids to the specified question set and re-save the question set xml with the
     * assigned guids.
     */
    File injectGuidsAndSave(set, qsetFile) {
        def qset = injectGuids(qsetFile.text)

        def file = new File("/data/workforce/metadata/question-set-with-guids-${set}.xml")
        def writer = new FileWriter(file)
        writer << XmlUtil.serialize(qset)
        writer.close()

        return file
    }

    /**
     * Inject any missing guids into a question set.
     */
    def injectGuids(fileContents) {
        def qset
        try {
            qset= new XmlSlurper().parseText(fileContents)
        } catch (IOException e) {
            println e
            return
        } catch (SAXException e) {
            println e
            return
        }

        injectIntoQuestions(qset.question, false)

        return qset
    }

    /**
     * Recursively injects missing guids into the specified list of questions.
     *
     * Only add guids to questions with an answer or where alwaysInject is true.
     * The param alwaysInject is used where the presence of an answer is implied by the question type
     * or the presence of a default answer property.
     *
     * @param questions the list of questions
     * @param alwaysInject if true add a guid regardless of whether an explicit answer is present
     */
    private void injectIntoQuestions(questions, alwaysInject) {
        questions.each {
            //println "Q-${it.text} ${it.answer.size() ? 'has' : 'has no'} answer"

            // add guid if the question has an answer
            if ((it.answer.size() || alwaysInject) && (!it.@id.toString())) {
                it.@id = UUID.randomUUID().toString()
            }

            // handle matrix questions - these not only imply an answer but also dynamically
            //  generate the child questions so the guids are stored as a list to be assigned
            //  to the questions when they are loaded
            if (it.@type == 'matrix' && it.data.guids.size() == 0) {
                // generate MxN guids
                def list = []
                int rows = it.data.rows.item.size()
                int cols = it.data.cols.item.size()
                // for matrix questions of type radio, the cols are the choices not separate questions
                // therefore the number of questions = the number of rows (aka cols = 1)
                if (it.answer.@type == 'radio') {
                    cols = 1
                }
                rows.times {
                    cols.times {
                        list << UUID.randomUUID().toString()
                    }
                }
                it.data.appendNode {
                    guids list.join(',')
                }
            }

            // call recursively on child questions
            if (it.question) {
                boolean always = false
                // handle default answers - these imply an answer for all child questions
                if (it.@defaultAnswerType.toString()) {
                    // assume all child questions will have an answer
                    always = true
                }
                injectIntoQuestions(it.question, always)
            }
        }
    }

    /**
     * Load from XML metadata
     */
    def loadQuestionSetXML(text) {
        def qset

        try {
            qset= new XmlSlurper().parseText(text)
        } catch (IOException e) {
            println e
            return
        } catch (SAXException e) {
            println e
            return
        }

        int set = loadSetMetadata(qset)
        loadInstitutionMetadata(qset, set)
        println "LoadQuestionSet ${set} ............."
        loadXmlQuestions(qset.question, set, 1, 0, 0, [:])
    }

    private loadInstitutionMetadata(qset, set) {
        def accounts = qset.accounts.account
        accounts.each { acc ->
            def inst = new Institution(account: acc.user.text(), code: acc.code.text(), name: acc.institution.text(),
                    uid: acc.uid.text(), setId: set)
            inst.save(flush:true)
            if (inst.hasErrors()) {
                println inst.errors
            }
        }
    }

    private int loadSetMetadata(qset) {
        int set = qset.@set.text().toInteger()
        def title = qset.title.text()
        def shortName = qset.shortName.text()
        def pageSequence = []
        if (qset.pageSequence.text()) {
            qset.pageSequence.page.each { page ->
                // text should be of the form m-n
                def values = page.text().tokenize('-')
                if (values.size() == 2) {
                    if (values[0].isInteger() && values[1].isInteger()) {
                        pageSequence << [from:values[0].toInteger(), to:values[1].toInteger()]
                    }
                }
            }
        }
        def qs = new QuestionSet(setId:set, title:title, shortName: shortName, pageSequence:(pageSequence as JSON).toString())
        if (!qs.save()) {
            qs.errors.each { println it }
        }

        return set
    }

    private void loadXmlQuestions(questions, set, level, level1, level2, Map defaults) {
        questions.eachWithIndex { it, idx ->
            // l1,l2,l3 are derived from the xml structure and used to label the question hierarchy
            // ident is a temp id string to provide scope for defaults

            def l1, l2, l3
            String ident
            switch (level) {
                case 1: l1 = idx + 1; l2 = 0; l3 = 0; ident=l1; break;
                case 2: l1 = level1; l2 = idx + 1; l3 = 0; ident=l1 + "_" + l2; break;
                case 3: l1 = level1; l2 = level2; l3 = idx + 1; ident=l1 + "_" + l2 + "_" + l3; break;
            }
            Question q = new Question(qset: set, level1: l1, level2: l2, level3: l3)
            // reset defaults for new top level question
            if (level == 1) {
                defaults = [:]
            }
            defaults = setDefaults(defaults, it, ident)

            q.guid = it.@id
            q.instruction = it.@instruction
            q.instructionPosition = it.@instructionPosition
            q.qtype = it.@type.toString() ? QuestionType.valueOf(it.@type.toString()) : QuestionType.none
            q.validation = it.validation
            if (!q.validation && q.qtype == QuestionType.rank) {
                q.validation = 'ranking-group'
            }
            q.label = it.label
            q.layoutHint = valueOrDefault(it.layoutHint, defaults, ident)
            q.displayHint = valueOrDefault(it.displayHint, defaults, ident)
            q.qdata = extractJsonString(it.data) as grails.converters.JSON
            q.qtext = it.text
            q.subtext = it.subtext
            q.shorttext = it.shortText
            def atype = valueOrDefault(it.answer?.@type, defaults, ident)
            q.atype = atype ? AnswerType.valueOf(atype as String) : AnswerType.none
            def datatype = valueOrDefault(it.answer?.@dataType, defaults, ident)

            if (datatype) {
                q.datatype = AnswerDataType.valueOf(datatype as String)
            } else {
                // default to what's appropriate for answer type
                switch (q.atype) {
                    case AnswerType.number:
                    case AnswerType.percent:
                    case AnswerType.rank:
                        q.datatype = AnswerDataType.number; break
                    case AnswerType.bool:
                        q.datatype = AnswerDataType.bool; break
                    default:
                        q.datatype = AnswerDataType.text
                }
            }
            q.required = it.answer?.@required == 'true' || it.answer?.@required == 'yes'
            q.requiredIf = it.answer?.@requiredIf
            q.adata = extractJsonString(it.answer?.data) as grails.converters.JSON
            q.alabel = it.answer?.label?.text()

            q.save()
            if (q.hasErrors()) {
                q.errors.each { error ->
                    println error
                }
            }

            // a question of type matrix is handled by automatically generating the inferred level 3 questions
            if (q.qtype == QuestionType.matrix) {
                /*
                These can be of 2 types:
                1. a list of m questions that have 1 answer each picked from a list of n options (in which case
                    the matrix quality applies to widget layout but does not multiply the number of answers)
                2. a genuine m x n matrix of answers
                */
                if (q.atype == AnswerType.radio) {
                    loadListOfQuestions(q, set, defaults)
                }
                else {
                    loadMatrixOfQuestions(q, set, defaults)
                }
            }

            // otherwise load child questions
            else if (it.question) {
                loadXmlQuestions(it.question, set, level + 1, l1, l2, defaults)
            }
        }
    }

    def loadMatrixOfQuestions(matrixQuestion, set, defaults) {
        def qdata = JSON.parse(matrixQuestion.qdata)
        def rows = qdata.rows
        def cols = qdata.cols
        assert qdata.guids : "no guids for question ${matrixQuestion.ident()}"
        def guids = qdata.guids.tokenize(',')
        def guidCounter = 0
        def questionIdx = 1

        // iterate through cells - rows first
        rows.each { row ->
            cols.each { col ->

                // auto-generate a question for this cell of the matrix
                Question q = new Question(qset: set, level1: matrixQuestion.level1, level2: matrixQuestion.level2, level3: questionIdx++)
                q.qtype = QuestionType.none
                q.datatype = AnswerDataType.number//valueOf(defaults.defaultDataType) as AnswerDataType
                q.atype = AnswerType.number//valueOf(defaults.defaultAnswerType) as AnswerType
                q.required = false //TODO for now
                q.adata = [row:row, col:col] as JSON
                q.guid = guids[guidCounter++]

                q.save()
                if (q.hasErrors()) {
                    q.errors.each { error ->
                        println error
                    }
                }
            }
        }
    }

    def loadListOfQuestions(matrixQuestion, set, defaults) {
        def qdata = JSON.parse(matrixQuestion.qdata)
        def rows = qdata.rows
        def guids = qdata.guids.tokenize(',')
        def guidCounter = 0

        // iterate through rows
        rows.eachWithIndex { row, questionIdx ->
            // questions numbers are one-based
            questionIdx++

            // auto-generate a question for this row of the matrix
            Question q = new Question(qset: set, level1: matrixQuestion.level1, level2: matrixQuestion.level2, level3: questionIdx++)
            q.qtype = QuestionType.none
            q.datatype = AnswerDataType.text//valueOf(defaults.defaultDataType) as AnswerDataType
            q.atype = AnswerType.text//valueOf(defaults.defaultAnswerType) as AnswerType
            q.qtext = row
            q.required = !(q.qtext.toLowerCase() =~ 'other') //TODO for now
            q.guid = guids[guidCounter++]

            q.save()
            if (q.hasErrors()) {
                q.errors.each { error ->
                    println error
                }
            }
        }
    }

    def extractJsonString(data) {
        if (!data || !data.size()) {
            return null
        }
        def result = null

        // check for single string
        if (data.children().size() == 0 && data.text()) {
            return [data.text()]
        }

        // determine if it's a list or qset of properties
        def name = data.children()[0].name()
        boolean isList = data.children().size() > 1
        if (isList) {
            data.children().each {
                if (it.name() != name) {
                    isList = false
                }
            }
        }
        
        if (isList) {
            //println "Processing list"
            result = []
            data.children().each {
                //println "item=${it.text()}"
                result << it.text()
            }
        } else {
            //println "Processing object"
            result = [:]
            use(MapUtil) {
                data.children().each {
                    //println "prop=${it.name()}=${it.text()}"
                    if (it.children().size()) {
                        result.addString(it, extractJsonString(it))
                    } else {
                        result.add(it)
                    }
                }
            }
        }

        return result
    }

    /**
     * Store any default values so they can be accessed by child questions.
     *
     * Use a key that is a combination of value type and the question ident. The latter makes
     * sure the values are only applied to child questions.
     *
     * @param defaults the map of default values
     * @param node xml node being processed
     * @param ident a string representation of the position of the question in the question hierarchy
     * @return the map with additional defaults from the question
     */
    def Map setDefaults(defaults, node, ident) {
        if (node.@defaultAnswerType.text()) {
            //println "Setting default answerType to ${node.@defaultAnswerType.text()}"
            defaults."${ident + 'defaultType'}" = node.@defaultAnswerType.text()
        }
        if (node.@defaultDataType.text()) {
            //println "Setting default dataType to ${node.@defaultDataType.text()}"
            defaults."${ident + 'defaultDataType'}" = node.@defaultDataType.text()
        }
        if (node.@defaultDisplayHint.text()) {
            //println "Setting default displayHint to ${node.@defaultDisplayHint.text()}"
            defaults."${ident + 'defaultDisplayHint'}" = node.@defaultDisplayHint.text()
        }
        if (node.@defaultLayoutHint.text()) {
            //println "Setting default layoutHint to ${node.@defaultLayoutHint.text()}"
            defaults."${ident + 'defaultLayoutHint'}" = node.@defaultLayoutHint.text()
        }
        return defaults
    }

    /**
     * Extract default values from the defaults map if there is no explicit value.
     *
     * Only use defaults set by a parent question.
     * @param node xml node being processed
     * @param defaults map of default values
     * @param ident a string representation of the position of the question in the question hierarchy
     * @return the explicit value or any applicable defaults
     */
    def valueOrDefault(node, defaults, ident) {
        def name = node.name()
        //println "name = " + name
        //defaults.each { key, value -> println "${key}=${value}"}
        if (node?.text()) {
            node.text()
        } else {
            def levels = ident.tokenize('_')
            switch (levels.size()) {
                case 1: // top level question so no defaults apply
                    return null
                case 2: // second level question try parent
                    def parentIdent = levels[0]
                    if (defaults."${parentIdent}default${name[0].toUpperCase()+name.substring(1)}") {
                        return defaults."${parentIdent}default${name[0].toUpperCase()+name.substring(1)}"
                    }
                    else {
                        return null
                    }
                case 3: // third level question try parent then grandparent
                    def parentIdent = levels[0]
                    if (defaults."${parentIdent}default${name[0].toUpperCase()+name.substring(1)}") {
                        return defaults."${parentIdent}default${name[0].toUpperCase()+name.substring(1)}"
                    }
                    else {
                        def grandparentIdent = levels[0] + '_' + levels[1]
                        if (defaults."${grandparentIdent}default${name[0].toUpperCase()+name.substring(1)}") {
                            return defaults."${grandparentIdent}default${name[0].toUpperCase()+name.substring(1)}"
                        }
                        else {
                            return null
                        }
                    }
            }
        }
    }

    /**
     * Load from JSON metadata
     */

    def loadQuestionSet(text, set) {
        def imp = JSON.parse(text)

        // clear existing
        def sql = new Sql(dataSource)
        sql.execute("delete from question where qset = ${set}")

        loadQuestions(imp.workforce, set, 1, 0, 0)
    }

    private void loadQuestions(questions, set, level, level1, level2) {
        questions.eachWithIndex { it, idx ->
//            println "Level ${level}: ${it.qtext}"
            def l1, l2, l3
            switch (level) {
                case 1: l1 = idx + 1; l2 = 0; l3 = 0; break;
                case 2: l1 = level1; l2 = idx + 1; l3 = 0; break;
                case 3: l1 = level1; l2 = level2; l3 = idx + 1; break;
            }
            Question q = new Question(qset: set, level1: l1, level2: l2, level3: l3)
            ['qtext','label','instruction','alabel','displayHint','qdata','adata','layoutHint'].each { key ->
                //println "${key} = " + it.(key)
                if (it."${key}") {
                    q."${key}" = it."${key}"
                }
            }
            q.qtype = it.qtype ? QuestionType.valueOf(it.qtype) : QuestionType.none
            q.atype = it.atype ? AnswerType.valueOf(it.atype) : AnswerType.none
            q.datatype = it.datatype ? AnswerDataType.valueOf(it.datatype) : AnswerDataType.text
            q.save()
            if (q.hasErrors()) {
                q.errors.each { error ->
                    println error
                }
            }
            if (it.question) {
                loadQuestions(it.question, set, level + 1, l1, l2)
            }
        }
    }

    def loadTestXML(text) {
        def model

        try {
            model = new XmlSlurper().parseText(text)
        } catch (IOException e) {
            println e
            return
        } catch (SAXException e) {
            println e
            return
        }

        println "text=" + model.text()
        println "no. data chilren is ${model.data.children().size()}"
    }

}

class MapUtil {
    def static addInt(map, node) {
        if (node.size()) {
            map."${node.name()}" = node.text() as int
        }
    }
    def static addString(map, node) {
        if (node.size()) {
            map."${node.name()}" = node.text()
        }
    }
    def static addString(map, node, text) {
        if (node.size()) {
            map."${node.name()}" = text
        }
    }
    def static add(map, node) {
        if (node.size()) {
            if (node.@value == 'int') {
                //println "Processing ${node.name()} as int"
                addInt(map, node)
            } else {
                //println "Processing ${node.name()} as string"
                addString(map, node)
            }
        }
    }

}
