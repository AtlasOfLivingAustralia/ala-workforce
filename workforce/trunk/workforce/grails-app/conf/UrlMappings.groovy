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

        "/report/$set/user/$id" (controller: 'report', action: 'answers')
        "/report/$set/q/$qid/user/$id" (controller: 'report', action: 'singleQuestionAnswer')

        "/admin/set/$set" (controller: 'admin', action: 'dashboard')

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
