(ns jiksnu.modules.json
  (:require [ciste.loader :refer [defhandler defmodule]]
            jiksnu.modules.json.views
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]
            [taoensso.timbre :as timbre]))

(defn require-components
  []
  (doseq [group-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "json" group-name)))

(defn start
  []
  (require-components))
