{:environment :development
 :modules ["jiksnu.commands"
           "jiksnu.factory"
           "jiksnu.db"
           "jiksnu.formats"
           "jiksnu.modules.web.routes"
           "jiksnu.workers"
           ]
 :modules-available
 [
  "jiksnu.commands"
  "jiksnu.factory"
  "jiksnu.workers"
  ]

 :services-available
 [
  "ciste.service.aleph"
  "ciste.services.nrepl"
  "ciste.service.swank"
  "ciste.service.tigase"
  "jiksnu.plugins.google-analytics"
  ]
 }
