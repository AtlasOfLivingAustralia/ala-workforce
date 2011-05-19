class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

        "/set/$set/page/$page" (controller: 'question', action: 'page')

        "/question/load/xml/$set" (controller: 'question', action: 'loadQuestionSetXML')
        "/question/$set/$id" (controller:'question', action:'singleQuestion')

        "/report/$set" (controller: 'report', action: 'answers')
        "/report/$set/q/$id" (controller: 'report', action: 'singleQuestionAnswer')

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
