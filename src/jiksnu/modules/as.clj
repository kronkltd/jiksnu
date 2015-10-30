(ns jiksnu.modules.as
  (:require [ciste.loader :as loader]
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]))

(defn require-components
  []
  (doseq [group-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "as" group-name)))

(defn start
  []
  (require-components))

(loader/defmodule "as"
  :start start
  :deps ["jiksnu.modules.json"])
