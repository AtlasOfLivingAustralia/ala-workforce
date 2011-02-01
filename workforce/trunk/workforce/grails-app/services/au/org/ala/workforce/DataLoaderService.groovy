package au.org.ala.workforce

import groovy.sql.Sql
import grails.converters.deep.JSON
import org.xml.sax.SAXException
import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.NodeChildren

class DataLoaderService {

    static transactional = false
    javax.sql.DataSource dataSource

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
        //println "class=" + qset.getClass()
        //println "size=" + qset.size()
        println "name=" + qset.name()

        // clear existing
        def sql = new Sql(dataSource)
        sql.execute("delete from question")

        loadXmlQuestions(qset.question, 1, 0, 0)
    }

    private void loadXmlQuestions(questions, level, level1, level2) {
        questions.eachWithIndex { it, idx ->
            println "Level ${level}: ${it.text}"
            def l1, l2, l3
            switch (level) {
                case 1: l1 = idx + 1; l2 = 0; l3 = 0; break;
                case 2: l1 = level1; l2 = idx + 1; l3 = 0; break;
                case 3: l1 = level1; l2 = level2; l3 = idx + 1; break;
            }
            Question q = new Question(level1: l1, level2: l2, level3: l3)
            //println it
            //println "it.class = " + it.getClass()
            //println "name=" + it.name
            //println "instruction = " + it.@instruction
            //println "qtype = " + it.@type
            q.instruction = it.@instruction
            q.qtype = it.@type.toString() ? QuestionType.valueOf(it.@type.toString()) : QuestionType.none
            q.label = it.@label
            q.layoutHint = it.layoutHint
            q.displayHint = it.displayHint
            q.qdata = it.data
            q.qtext = it.text

            //println "datatype = " + it.answer?.@dataType
            q.datatype = it.answer?.@dataType?.toString() ? AnswerDataType.valueOf(it.answer?.@dataType?.toString()) : AnswerDataType.text
            q.alabel = it.answer?.@label
            q.atype = (it.answer?.@type as String) ? AnswerType.valueOf(it.answer?.@type as String) : AnswerType.none
            if (it.answer?.data) {
                def map = transformToObject(it.answer?.data)
                if (map?.data) {
                    println ">>>adata>>>" + map
                    /*map.each { key, value ->
                        println ">${key} = ${value}"
                    }*/
                    println ">>>data " + map.data
                    if (map.data instanceof String) {
                        q.adata = map.data
                    } else {
                        def json = (map.data as grails.converters.JSON).toString()
                        println json
                        try {
                            q.adata = json
                        } catch (MissingPropertyException e) {
                            println e
                            q.adata = json
                        }
                    }
                }
            }

            q.save()
            if (q.hasErrors()) {
                q.errors.each { error ->
                    println error
                }
            }
            if (it.question) {
                loadXmlQuestions(it.question, level + 1, l1, l2)
            }
        }
    }

    /**
     * Assuming here we have no mixed content, ie content is either text or a node list
     * and no attributes.
     * @param node
     * @return JSON object
     */
    def transformToObject(node) {
        def map = [:]
        if (node.childNodes()) {
            map."${node.name().toString()}" = transformToList(node.childNodes())
        } else {
            map."${node.name()}" = [node.text()]
        }
        return map
    }

    def transformToList(children) {
        def list = []
        children.each {
            list << transformToObject(it)
        }
        return list
    }
    
    def debugNode(node, int depth) {
        if (!node) { return null }
        def indent = ""
        depth.times { indent += "  "}

        if (node.getClass().getSimpleName() == 'NodeChildren') {
            def list = []
            node.each {
                list << debugNode(it, depth + 1)
            }
            return list
        } else {
            def map = [:]
            println indent + "{" + node.name() + ":"
            def objName = node.name()
            def objList = [:]
            /*node.attributes().each { key, value ->
                println indent + "  ${key}:${value},"
                objList."${key}" = value
            }*/
            def children = node.childNodes()
            if (children) {
                children.each {
                    objList << debugNode(it, depth + 1)
                }
            } else {
                if (node.text()) {
                    println indent + node.text()
                    objList = node.text()
                }
            }
            return objList
        }
    }

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
}
