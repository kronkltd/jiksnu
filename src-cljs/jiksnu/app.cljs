(ns jiksnu.app
  (:require [jiksnu.helpers :as helpers])
  (:use-macros [gyr.core :only [def.module def.config]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def.module jiksnu
  [
   angular-data.DSCacheFactory
   angularFileUpload
   angularMoment
   btford.markdown
   cgNotify
   cgBusy
   geolocation
   datatables
   ngSanitize
   cfp.hotkeys
   ui.bootstrap
   ;; ui.bootstrap.tabs
   ui.router
   uiGmapgoogle-maps
   ws
   ])

