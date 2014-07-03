{:environment :development

 :modules ["jiksnu.core"
           "jiksnu.modules.core"
           "jiksnu.modules.web"]

 :modules-available []

 :services-available
 [
  "ciste.service.aleph"
  "ciste.services.nrepl"
  "ciste.service.swank"
  "jiksnu.plugins.google-analytics"
  ]
 }
