(ns jiksnu.modules.core
  (:require [ciste.initializer :refer [definitializer]]
            [clojure.tools.logging :as log]
            jiksnu.modules.core.formats
            jiksnu.modules.core.views
            [jiksnu.db :as db]
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]))

(def module
  {:name "jiksnu-core"
   :deps []})

(defn start
  []
  (log/info "starting core")
  (db/set-database!)

  (doseq [model-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "core" model-name)))

