package au.org.ala.workforce

import groovy.sql.Sql
import grails.converters.deep.JSON
import org.xml.sax.SAXException

class DataLoaderService {

    static transactional = false
    javax.sql.DataSource dataSource

    /**
     * Load from XML metadata
     */

    def loadQuestionSetXML(text) {
        println "LoadQuestionSetXML............."
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
        // clear existing
        def sql = new Sql(dataSource)
        sql.execute("delete from question")

        loadXmlQuestions(qset.question, 1, 0, 0, [:])
    }

    private void loadXmlQuestions(questions, level, level1, level2, Map defaults) {
        questions.eachWithIndex { it, idx ->
            //println "Level ${level}: ${it.text}"
            def l1, l2, l3
            switch (level) {
                case 1: l1 = idx + 1; l2 = 0; l3 = 0; break;
                case 2: l1 = level1; l2 = idx + 1; l3 = 0; break;
                case 3: l1 = level1; l2 = level2; l3 = idx + 1; break;
            }
            Question q = new Question(level1: l1, level2: l2, level3: l3)
            // reset defaults for new top level question
            if (level == 1) {
                defaults = [:]
            }
            defaults = setDefaults(defaults, it)

            q.instruction = it.@instruction
            q.qtype = it.@type.toString() ? QuestionType.valueOf(it.@type.toString()) : QuestionType.none
            q.label = it.label
            q.layoutHint = valueOrDefault(it.layoutHint, defaults)
            q.displayHint = valueOrDefault(it.displayHint, defaults)
            q.qdata = extractJsonString(it.data) as grails.converters.JSON
            q.qtext = it.text
            q.atype = valueOrDefault(it.answer?.@type, defaults) ? AnswerType.valueOf(valueOrDefault(it.answer?.@type, defaults) as String) : AnswerType.none
            def datatype = null//TODO:valueOrDefault(it.answer?.@dataType, defaults)
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
            q.adata = extractJsonString(it.answer?.data) as grails.converters.JSON
            q.alabel = it.answer?.label?.text()

            q.save()
            if (q.hasErrors()) {
                q.errors.each { error ->
                    println error
                }
            }
            if (it.question) {
                loadXmlQuestions(it.question, level + 1, l1, l2, defaults)
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

        // determine if it's a list or set of properties
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

    def loadQuestionSet(text) {
        def imp = JSON.parse(text)

        // clear existing
        def sql = new Sql(dataSource)
        sql.execute("delete from question")

        loadQuestions(imp.workforce, 1, 0, 0)
    }

    private void loadQuestions(questions, level, level1, level2) {
        questions.eachWithIndex { it, idx ->
//            println "Level ${level}: ${it.qtext}"
            def l1, l2, l3
            switch (level) {
                case 1: l1 = idx + 1; l2 = 0; l3 = 0; break;
                case 2: l1 = level1; l2 = idx + 1; l3 = 0; break;
                case 3: l1 = level1; l2 = level2; l3 = idx + 1; break;
            }
            Question q = new Question(level1: l1, level2: l2, level3: l3)
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
                loadQuestions(it.question, level + 1, l1, l2)
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
