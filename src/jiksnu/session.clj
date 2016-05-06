(ns jiksnu.session
  (:require [jiksnu.model.user :as model.user]))

(def ^:dynamic *current-user-id* (ref nil))

(defn current-user-id
  []
  @*current-user-id*)

(defn current-user
  []
  (if-let [id (current-user-id)]
    (model.user/fetch-by-id id)))

(defn is-admin?
  ([]
   (if-let [user (current-user)]
     (is-admin? user)
     false))
  ([user]
   (:admin user)))

(defmacro with-user-id
  [id & body]
  `(binding [*current-user-id* (ref ~id)]
     ~@body))

(defmacro with-user
  [user & body]
  `(with-user-id (:_id ~user) ~@body))

(defn set-authenticated-user!
  [user]
  (dosync
   (ref-set *current-user-id* (:_id user)))
  user)
