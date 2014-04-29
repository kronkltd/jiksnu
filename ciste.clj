{:environment :development
 :modules ["jiksnu.commands"
           "jiksnu.factory"
           "jiksnu.db"
           "jiksnu.formats"
           "jiksnu.routes"
           "jiksnu.workers"
           "jiksnu.triggers.domain-triggers"
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
