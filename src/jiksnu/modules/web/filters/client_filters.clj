(ns jiksnu.modules.web.filters.client-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.client-actions :as actions.client]
            [slingshot.slingshot :refer [throw+]]))

(deffilter #'actions.client/index :http
  [action request]
  (action))

(deffilter #'actions.client/index :page
  [action request]
  (action))

(deffilter #'actions.client/register :http
  [action request]
  (if-let [body (:body request)]
    (let [body-str (slurp body)
          params (merge (:params request)
                        (json/read-str body-str :key-fn keyword))]
      (action params))
    (throw+ "body not provided")))
