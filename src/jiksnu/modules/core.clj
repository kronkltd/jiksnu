(ns jiksnu.modules.core
  (:require jiksnu.modules.core.formats
            jiksnu.modules.core.views
            [jiksnu.db :as db]
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]))

(def module
  {:name "jiksnu-core"
   :deps []})

(defn start
  []
  (db/set-database!)

  (doseq [model-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "core" model-name)))
