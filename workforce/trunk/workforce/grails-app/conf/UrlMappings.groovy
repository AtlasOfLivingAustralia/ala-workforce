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

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
