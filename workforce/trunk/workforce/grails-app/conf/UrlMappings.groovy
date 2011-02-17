class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

        "/question/$id" (controller:'question', action:'singleQuestion')
        "/questions/$from?/$to?" (controller:'question', action:'allQuestions')

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
