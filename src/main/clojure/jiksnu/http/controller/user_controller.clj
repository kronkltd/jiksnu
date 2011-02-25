(ns jiksnu.http.controller.user-controller
  (:use jiksnu.config
        jiksnu.model
        [jiksnu.session :only (current-user)]
        clojure.contrib.logging)
  (:require [jiksnu.model.user :as model.user]))

(defn index
  [request]
  (model.user/index))

(defn show
  [{{id "id"} :params
    :as request}]
  (model.user/show id))

(defn delete
  [{{id "id"} :params}]
  (model.user/delete id)
  true)

(defn create
  [{{username "username"
     password "password"
     confirm-password "confirm_password"} :params
     :as request}]
  (if (and username password confirm-password)
    (if (= password confirm-password)
      (do
        (model.user/create {:username username
                            :domain (:domain (config))
                            :password password
                            :confirm_password password})))))

(defn update
  [{{username "username" :as params} :params :as request}]
  (let [user (model.user/show username (:domain (config)))]
    (let [new-params
          (-> (into {}
                    (map
                     (fn [[k v]]
                       (if (not= v "")
                         [(keyword k) v]))
                     params))
              (dissoc :id)
              #_(assoc :_id id))]
      (model.user/update new-params))))

(defn edit
  [request]
  (let [user (show request)]
    user))

(defn register
  [request]
  true)

(defn profile
  [request]
  (if-let [user (current-user)]
    user (error "no user")))

(defn subscriptions
  [request]
  [])

(defn subscribers
  [request]
  [])

(defn remote-profile
  [request]
  (let [{{id "id"} :params} request]
    (let [user (model.user/fetch-by-id id)]
      user)))

