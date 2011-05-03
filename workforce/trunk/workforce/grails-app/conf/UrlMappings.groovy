class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

        "/question/load/xml/$set" (controller: 'question', action: 'loadQuestionSetXML')
        "/question/load/json/$set" (controller: 'question', action: 'loadQuestionSet')
        "/question/$set/$id" (controller:'question', action:'singleQuestion')
        "/questions/$set/$from?" (controller:'question', action:'questions')
        "/questions/$set/$from?/$to?" (controller:'question', action:'questions')

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
