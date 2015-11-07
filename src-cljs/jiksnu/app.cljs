(ns jiksnu.app
  (:use-macros [gyr.core :only [def.module]]))

(def.module jiksnu
  [
   angularFileUpload
   ;; angularMoment
   btford.markdown
   cgNotify
   geolocation
   datatables
   hljs
   js-data
   ngSanitize
   ngWebSocket
   cfp.hotkeys
   ui.bootstrap
   ui.router
   ])
