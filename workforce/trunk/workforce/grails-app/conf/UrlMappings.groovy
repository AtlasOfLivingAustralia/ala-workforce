class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

        "/question/load/xml" (controller: 'question', action: 'loadQuestionSetXML')
        "/question/load/json" (controller: 'question', action: 'loadQuestionSet')
        "/question/$id" (controller:'question', action:'singleQuestion')
        "/questions/$from?" (controller:'question', action:'questions')
        "/questions/$from?/$to?" (controller:'question', action:'questions')

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
