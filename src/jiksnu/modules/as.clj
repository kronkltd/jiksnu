(ns jiksnu.modules.as
  (:require [ciste.loader :as loader]
            jiksnu.modules.as.sections
            jiksnu.modules.as.views))

(defn start [])

(loader/defmodule "as"
  :start start
  :deps ["jiksnu.modules.json"])
