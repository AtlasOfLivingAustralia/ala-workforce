package au.org.ala.workforce

class SurveyController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def newSurvey = {
        def surveyInstance = new Survey()
        surveyInstance.properties = params
        render(view:'newSurvey', model:[qset: QuestionSet.findBySetId(params.set), surveyInstance: surveyInstance])
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [surveyInstanceList: Survey.list(params), surveyInstanceTotal: Survey.count()]
    }

    def create = {
        def surveyInstance = new Survey()
        surveyInstance.properties = params
        return [surveyInstance: surveyInstance]
    }

    def save = {
        def surveyInstance = Survey.findByYearAndType(params.year, params.surveyType)
        if (surveyInstance) {
            flash.message = "Survey already exists"
            redirect(action:'newSurvey', params: params)
        } else {
            surveyInstance = new Survey(year: params.year, priorYear1: params.priorYear1, priorYear2: params.priorYear2, 'type': params.surveyType)
            if (surveyInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.created.message', args: [message(code: 'survey.label', default: 'Survey'), surveyInstance.id])}"
                redirect(action: "list")
            }
            else {
                render(view: "create", model: [surveyInstance: surveyInstance])
            }
        }
    }

    def show = {
        def surveyInstance = Survey.get(params.id)
        if (!surveyInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'survey.label', default: 'Survey'), params.id])}"
            redirect(action: "list")
        }
        else {
            [surveyInstance: surveyInstance]
        }
    }

    def edit = {
        def surveyInstance = Survey.get(params.id)
        if (!surveyInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'survey.label', default: 'Survey'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [surveyInstance: surveyInstance]
        }
    }

    def update = {
        def surveyInstance = Survey.get(params.id)
        if (surveyInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (surveyInstance.version > version) {

                    surveyInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'survey.label', default: 'Survey')] as Object[], "Another user has updated this Survey while you were editing")
                    render(view: "edit", model: [surveyInstance: surveyInstance])
                    return
                }
            }
            surveyInstance.properties = params
            if (!surveyInstance.hasErrors() && surveyInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'survey.label', default: 'Survey'), surveyInstance.id])}"
                redirect(action: "show", id: surveyInstance.id)
            }
            else {
                render(view: "edit", model: [surveyInstance: surveyInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'survey.label', default: 'Survey'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def surveyInstance = Survey.get(params.id)
        if (surveyInstance) {
            try {
                surveyInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'survey.label', default: 'Survey'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'survey.label', default: 'Survey'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'survey.label', default: 'Survey'), params.id])}"
            redirect(action: "list")
        }
    }
}
