package au.org.ala.workforce

import groovy.sql.Sql
import grails.converters.deep.JSON
import org.xml.sax.SAXException

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
            println it
            println "it.class = " + it.getClass()
            println "name=" + it.name
            println "instruction = " + it.@instruction
            println "qtext = " + it.text
            q.instruction = it.@instruction
            q.qtext = it.text

            /*['qtext','label','instruction','alabel','displayHint','qdata','adata','layoutHint'].each { key ->
                //println "${key} = " + it.(key)
                if (it."${key}") {
                    q."${key}" = it."${key}"
                }
            }*/
            /*q.qtype = it.@qtype ? QuestionType.valueOf(it.@qtype) : QuestionType.none
            q.atype = it.atype ? AnswerType.valueOf(it.atype) : AnswerType.none
            q.datatype = it.@datatype ? AnswerDataType.valueOf(it.@datatype) : AnswerDataType.text
            q.save()
            if (q.hasErrors()) {
                q.errors.each { error ->
                    println error
                }
            }*/
            if (it.question) {
                loadXmlQuestions(it.question, level + 1, l1, l2)
            }
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
