(ns jiksnu.app
  (:use-macros [gyr.core :only [def.module]]))

(.. js/Raven
    (config js/sentryDSNClient)
    (addPlugin (.. js/Raven -Plugins -Angular))
    (install))

(def.module jiksnu
  [angular-clipboard
   angularMoment
   btford.markdown
   cfp.hotkeys
   geolocation
   hljs
   js-data
   lfNgMdFileInput
   #_
   ngRaven
   ngMaterial
   ngMdIcons
   ngSanitize
   ngWebSocket
   ui.router
   ui.select])
