{:environment :development

 :modules [
           "jiksnu.core"
           ;; "jiksnu.modules.admin"
           ;; "jiksnu.modules.as"
           "jiksnu.modules.command"
           ;; "jiksnu.modules.core"
           ;; "jiksnu.modules.json"
           "jiksnu.modules.http"
           "jiksnu.modules.web"
           ]
 :services [
            "ciste.services.http-kit"
            "ciste.services.nrepl"
            "jiksnu.plugins.google-analytics"
            ]
 }
