(ns jiksnu.modules.http
  (:require [ciste.loader :refer [defmodule]]
            [taoensso.timbre :as log]
            jiksnu.modules.core.formats
            jiksnu.modules.core.views))

(defn start
  []
  (log/info "starting http"))

(defmodule "http"
  :start start
  :deps ["jiksnu.modules.core"])
