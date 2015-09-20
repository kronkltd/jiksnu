(ns jiksnu.modules.web
  (:require [ciste.loader :as loader
             :refer [defhandler defmodule]]
            [clojure.tools.logging :as log]
            jiksnu.modules.web.formats
            [jiksnu.modules.web.handlers :as handlers]
            [jiksnu.modules.web.helpers :as helpers]
            jiksnu.plugins.google-analytics
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]))

(defn require-components
  []
  (doseq [group-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "web" group-name))
  (helpers/load-routes)
  (require 'jiksnu.modules.web.routes))

(defn start
  []
  ;; (log/info "starting web")
  (handlers/init-handlers)
  (require-components))

(defmodule "web"
  :start start
  :deps ["jiksnu.modules.core"
         "jiksnu.modules.json"])
