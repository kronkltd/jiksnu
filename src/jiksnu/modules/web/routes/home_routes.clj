(ns jiksnu.modules.web.routes.home-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.site-actions :as site]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.modules.web.routes :as r]
            [octohipster.core :refer [defresource defgroup]]
            [octohipster.mixins :as mixin]))

(defresource root home
  :url "/"
  :available-media-types ["text/html"]
  :handle-ok helpers/index
  :doc {:get {:nickname "home-page"
              :summary "Home Page"}})

(defresource root status
  :url "/status"
  :available-media-types ["application/json"]
  :handle-ok (fn [request]
               (log/spy :info (json/json-str (site/status))))
  )

(defgroup root
  :url ""
  )
