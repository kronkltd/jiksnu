(ns jiksnu.modules.http
  (:require [ciste.loader :as loader
             :refer [defhandler defmodule]]
            [clojure.tools.logging :as log]
            ;; FIXME: If core is loaded properly, this is not needed
            jiksnu.modules.core.formats
            jiksnu.modules.core.views

            ))

(defn start
  []
  ;; (log/info "starting http")
  )

(defmodule "http"
  :start start
  :deps ["jiksnu.modules.core"])

