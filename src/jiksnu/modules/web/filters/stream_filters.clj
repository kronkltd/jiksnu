(ns jiksnu.modules.web.filters.stream-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [try+]]))

(deffilter #'actions.stream/create :http
  [action request]
  (let [params (:params request)]
    (action params)))
