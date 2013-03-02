{:environment :development
 :modules ["jiksnu.commands"
           "jiksnu.factory"
           "jiksnu.workers"
           "jiksnu.views"]
 :modules-available
 [
  "jiksnu.commands"
  "jiksnu.factory"
  "jiksnu.workers"
  "jiksnu.views"
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
