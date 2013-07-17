(ns jiksnu.session
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]))

(def ^:dynamic *current-user-id* (ref nil))
(def ^:dynamic *admin-mode* false)

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
     (or *admin-mode* (:admin user))))

(defmacro with-user-id
  [id & body]
  `(binding [*current-user-id* (ref ~id)]
     ~@body))

(defmacro with-user
  [user & body]
  `(with-user-id (:_id ~user) ~@body))

(defmacro with-admin
  [& body]
  `(binding [*admin-mode* true]
     ~@body))

(defn set-authenticated-user!
  [user]
  (dosync
   (ref-set *current-user-id* (:_id user)))
  user)
