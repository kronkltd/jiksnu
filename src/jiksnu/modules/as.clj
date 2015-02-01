(ns jiksnu.modules.as
  (:require [ciste.loader :as loader
             :refer [defhandler defmodule]]
            [clojure.tools.logging :as log]
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]))

(defn require-components
  []
  (doseq [group-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "as" group-name)))

(defn start
  []
  (log/info "starting AS")
  (require-components)
)

(defmodule "as"
  :start start
  :deps [
         "jiksnu.modules.json"
         ]
  )
