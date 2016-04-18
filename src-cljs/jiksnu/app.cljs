(ns jiksnu.app
  (:use-macros [gyr.core :only [def.module]]))

(def sentry-dsn-client "http://68981c8a90cb4f079bc84dff62851d16@sentry.docker/2")

(.. js/Raven
    (config sentry-dsn-client)
    (addPlugin (.. js/Raven -Plugins -Angular))
    (install))

(def.module jiksnu
  [
   angular-clipboard
   angularFileUpload
   angularMoment
   btford.markdown
   geolocation
   datatables
   hljs
   js-data
   ngRaven
   ngSanitize
   ngWebSocket
   cfp.hotkeys
   ui.bootstrap
   ui-notification
   ui.router
   ui.select
   ])
