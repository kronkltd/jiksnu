(ns jiksnu.modules.http
  (:require [ciste.loader :refer [defmodule]]
            jiksnu.modules.core.formats
            jiksnu.modules.core.views
            [taoensso.timbre :as timbre]))

(defn start
  []
  #_(timbre/info "starting http"))

(defmodule "http"
  :start start
  :deps ["jiksnu.modules.core"])
