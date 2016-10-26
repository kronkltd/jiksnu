(ns jiksnu.modules.as
  (:require [ciste.loader :as loader]
            jiksnu.modules.as.sections
            jiksnu.modules.as.views
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]))

(defn start [])

(loader/defmodule "as"
  :start start
  :deps ["jiksnu.modules.json"])
