{:environment :development
 :logger "jiksnu.modules.web.logger"
 :modules [
           "jiksnu.core"
           "jiksnu.modules.command"
           "jiksnu.modules.http"
           "jiksnu.modules.web"
           ]
 :services [
            "ciste.services.http-kit"
            "ciste.services.nrepl"
            "jiksnu.plugins.google-analytics"
            ]
 }
