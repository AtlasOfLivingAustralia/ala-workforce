package au.org.ala.workforce

class SessionController {

    def logout = {
        session.invalidate()
        redirect(url:"${params.casUrl}?url=${params.appUrl}")
    }
    
    def index = { }
}
