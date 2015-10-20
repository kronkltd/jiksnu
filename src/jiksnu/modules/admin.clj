(ns jiksnu.modules.admin
  (:require [ciste.loader :as loader
             :refer [defhandler defmodule]]
            [taoensso.timbre :as log]
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]))

(defn require-components
  []
  (doseq [group-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "admin" group-name)))

(defn start
  []
  (require-components))

(defmodule "as"
  :start start
  :deps ["jiksnu.modules.json"])
