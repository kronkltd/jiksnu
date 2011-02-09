(ns jiksnu.session
  (:require [jiksnu.model.user :as model.user]))

(def #^:dynamic *current-user* nil)
(def #^:dynamic *admin-mode* false)

(defn current-user-id
  []
  *current-user*)

(defn current-user
  []
  (if-let [id (current-user-id)]
    (model.user/show id)))

(defn is-admin?
  ([]
     (if-let [user (current-user)]
       (is-admin? user)
       false))
  ([user]
     (or *admin-mode* (:admin user))))

(defmacro with-user
  [username & body]
  `(binding [jiksnu.session/*current-user* ~username]
     ~@body))

(defmacro with-admin
  [& body]
  `(binding [jiksnu.session/*admin-mode* true]
    ~@body))
