package au.org.ala.workforce

import org.xml.sax.SAXException
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext

class ListLoaderService implements ApplicationContextAware {

    static transactional = false

    static states = []
    static universities = []
    ApplicationContext applicationContext

    def load() {
        if (!states) {
            def loadFile = new File("/data/workforce/metadata/lists.xml")
            if (!loadFile.exists()) {
                loadFile = applicationContext.getResource("metadata/lists.xml").getFile()
            }
            assert loadFile
            assert loadFile.exists()

            try {
                def lists = new XmlSlurper().parseText(loadFile.text)
                // turn the node lists into java array lists
                lists.statesList.item.each {
                    states << it.toString()
                }
                lists.universities.item.each {
                    universities << it.toString()
                }
            } catch (IOException e) {
                println e
            } catch (SAXException e) {
                println e
            }
        }
    }

}
