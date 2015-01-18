(ns jiksnu.modules.web.routes.home-routes
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.modules.web.routes :as r]
            [octohipster.core :refer [defresource defgroup]]
            [octohipster.mixins :as mixin]))

(defresource home
  :url "/"
  :available-media-types ["text/html"]
  :handle-ok helpers/index
  :doc {:get {:nickname "home-page"
              :summary "Home Page"}})


(defgroup home-group
  :url ""
  :resources [home])

(defn on-loaded
  []
  (log/info "adding home group")
  (dosync
   (alter r/groups conj home-group)))

