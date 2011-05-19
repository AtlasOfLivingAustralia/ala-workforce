package au.org.ala.workforce

import groovy.sql.Sql
import grails.converters.JSON
import org.xml.sax.SAXException

class DataLoaderService {

    static transactional = false
    javax.sql.DataSource dataSource

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
        println "LoadQuestionSet ${set} ............."
        loadXmlQuestions(qset.question, set, 1, 0, 0, [:])
    }

    private int loadSetMetadata(qset) {
        int set = qset.@set.text().toInteger()
        def title = qset.title.text()
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
        def qs = new QuestionSet(setId:set, title:title, pageSequence:(pageSequence as JSON).toString())
        if (!qs.save()) {
            qs.errors.each { println it }
        }

        return set
    }

    private void loadXmlQuestions(questions, set, level, level1, level2, Map defaults) {
        questions.eachWithIndex { it, idx ->
            //println "Set ${set} Level ${level}: ${it.text}"
            def l1, l2, l3
            switch (level) {
                case 1: l1 = idx + 1; l2 = 0; l3 = 0; break;
                case 2: l1 = level1; l2 = idx + 1; l3 = 0; break;
                case 3: l1 = level1; l2 = level2; l3 = idx + 1; break;
            }
            Question q = new Question(qset: set, level1: l1, level2: l2, level3: l3)
            // reset defaults for new top level question
            if (level == 1) {
                defaults = [:]
            }
            defaults = setDefaults(defaults, it)

            q.instruction = it.@instruction
            q.qtype = it.@type.toString() ? QuestionType.valueOf(it.@type.toString()) : QuestionType.none
            q.validation = it.validation
            if (!q.validation && q.qtype == QuestionType.rank) {
                q.validation = 'ranking-group'
            }
            q.label = it.label
            q.layoutHint = valueOrDefault(it.layoutHint, defaults)
            q.displayHint = valueOrDefault(it.displayHint, defaults)
            if (it.@heightHint.text()) {
                q.heightHint = it.@heightHint.text() as int
            }
            q.qdata = extractJsonString(it.data) as grails.converters.JSON
            q.qtext = it.text
            q.shorttext = it.shortText
            q.atype = valueOrDefault(it.answer?.@type, defaults) ? AnswerType.valueOf(valueOrDefault(it.answer?.@type, defaults) as String) : AnswerType.none
            def datatype = valueOrDefault(it.answer?.@dataType, defaults)

            /* --Special hack--
             * Questions of type 'group' that contain questions with boolean answers use defaultDatatype=bool
             * to save declaring datatype on every answer.
             * However this default is applied to the question in which it is defined which is not intended (here
             * at least) and which activates the 'checkbox blocking rule' ie. the processor thinks the child answers
             * are irrelevant because parent is a boolean which is not 'on'.
             * TODO: The fix is to refactor so that default does not apply to the question where it is declared - however
             * needs extensive testing as it may break other scenarios.
             * So for the moment just set the datatype and answer type of group questions to avoid the issue.
             */
            if (q.qtype == QuestionType.group) {
                datatype = 'text'
                q.atype = AnswerType.none
            }

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
        def questionIdx = 1

        // iterate through cells - rows first
        rows.each { row ->
            cols.each { col ->

                // auto-generate a question for this cell of the matrix
                Question q = new Question(qset: set, level1: matrixQuestion.level1, level2: matrixQuestion.level2, level3: questionIdx++)
                q.qtype = QuestionType.none
                q.datatype = AnswerDataType.number//valueOf(defaults.defaultDataType) as AnswerDataType
                q.atype = AnswerType.number//valueOf(defaults.defaultAnswerType) as AnswerType
                q.required = true //TODO for now
                q.adata = [row:row, col:col] as JSON

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

        // iterate through rows
        rows.eachWithIndex { row, questionIdx ->
            // questions numbers are one-based
            questionIdx++

            // auto-generate a question for this row of the matrix
            Question q = new Question(qset: set, level1: matrixQuestion.level1, level2: matrixQuestion.level2, level3: questionIdx++)
            q.qtype = QuestionType.none
            q.datatype = AnswerDataType.text//valueOf(defaults.defaultDataType) as AnswerDataType
            q.atype = AnswerType.text//valueOf(defaults.defaultAnswerType) as AnswerType
            q.required = true //TODO for now
            q.qtext = row

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

    def Map setDefaults(defaults, node) {
        if (node.@defaultAnswerType.text()) {
            //println "Setting default answerType to ${node.@defaultAnswerType.text()}"
            defaults.defaultType = node.@defaultAnswerType.text()
        }
        if (node.@defaultDataType.text()) {
            //println "Setting default dataType to ${node.@defaultDataType.text()}"
            defaults.defaultDataType = node.@defaultDataType.text()
        }
        if (node.@defaultDisplayHint.text()) {
            //println "Setting default displayHint to ${node.@defaultDisplayHint.text()}"
            defaults.defaultDisplayHint = node.@defaultDisplayHint.text()
        }
        if (node.@defaultLayoutHint.text()) {
            //println "Setting default layoutHint to ${node.@defaultLayoutHint.text()}"
            defaults.defaultLayoutHint = node.@defaultLayoutHint.text()
        }
        return defaults
    }

    def valueOrDefault(node, defaults) {
        def name = node.name()
        //println "name = " + name
        //defaults.each { key, value -> println "${key}=${value}"}
        if (node?.text()) {
            node.text()
        } else if (defaults."default${name[0].toUpperCase()+name.substring(1)}") {
            return defaults."default${name[0].toUpperCase()+name.substring(1)}"
        } else {
            return null
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
