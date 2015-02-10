(ns jiksnu.modules.http
  (:require [ciste.loader :as loader
             :refer [defhandler defmodule]]
            [clojure.tools.logging :as log]))

(defn start
  []
  ;; (log/info "starting http")
  )

(defmodule "http"
  :start start
  :deps ["jiksnu.modules.core"])

