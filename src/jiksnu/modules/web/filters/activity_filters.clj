(ns jiksnu.modules.web.filters.activity-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.data.json :as json]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [slingshot.slingshot :refer [try+]]))

(deffilter #'actions.activity/delete :http
  [action request]
  (if-let [id (try+ (-> request :params :id)
                    (catch RuntimeException ex
                      ;; FIXME: handle error
                      ))]
    (if-let [activity (model.activity/fetch-by-id id)]
      (action activity))))

(deffilter #'actions.activity/post :http
  [action request]
  (let [body-params (when-let [body (:body request)]
                      (when-let [body-str (slurp body)]
                        (when-not (= body-str "")
                          (json/read-str body-str))))
        params (->> (-> request :params
                        (merge body-params)
                        (dissoc "geo.latitude"))
                    (map (fn [[k v]]
                           [(keyword k) v]
                           ))
                    (into {}))]
    (action params)))
