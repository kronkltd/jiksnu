(ns jiksnu.modules.admin
  (:require [ciste.loader :refer [defmodule]]))

(defn start [])

(defn stop
  [])

(defmodule "jiksnu.modules.admin"
  :start start
  :deps ["jiksnu.modules.json"])
