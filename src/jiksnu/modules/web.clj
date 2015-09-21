(ns jiksnu.modules.web
  (:require [ciste.loader :as loader
             :refer [defhandler defmodule]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.http.resources :refer [init-site-reloading!]]
            jiksnu.modules.web.formats
            [jiksnu.modules.web.handlers :as handlers]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.modules.web.core :refer [jiksnu-init]]
            jiksnu.plugins.google-analytics
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]))

(defn require-components
  []
  (doseq [group-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "web" group-name))
  (helpers/load-routes))

(defn start
  []
  ;; (log/info "starting web")
  (handlers/init-handlers)
  (require-components)
  (init-site-reloading! jiksnu-init)
  (jiksnu-init))

(defmodule "web"
  :start start
  :deps ["jiksnu.modules.core"
         "jiksnu.modules.json"])
