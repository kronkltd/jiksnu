(ns jiksnu.modules.web
  (:require [ciste.loader :refer [defmodule]]
            [jiksnu.modules.http.resources :refer [init-site-reloading!]]
            jiksnu.modules.web.formats
            [jiksnu.modules.web.handlers :as handlers]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.modules.web.core :refer [jiksnu jiksnu-init]]))

(defn start
  []
  (handlers/init-handlers)
  (helpers/load-routes)
  (jiksnu-init)
  (init-site-reloading! #'jiksnu))

(defn stop [])

(defmodule "jiksnu.modules.web"
  :start start
  :deps ["jiksnu.modules.core"
         "jiksnu.modules.json"])
