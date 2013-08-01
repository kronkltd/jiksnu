(ns jiksnu.filters.admin.worker-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.admin.worker-actions))

(deffilter #'index :command
  [action request]
  (action))

(deffilter #'stop-worker :command
  [action request]
  (-> request :args first Integer/parseInt action)
  true)





(deffilter #'index :http
  [action request]
  (action))

(deffilter #'start-worker :command
  [action request]
  (apply action (:args request)))

(deffilter #'start-worker :http
  [action request]
  (-> request :params :name action))

(deffilter #'stop-worker :http
  [action request]
  (-> request :params :id Integer/parseInt action))

(deffilter #'stop-all-workers :http
  [action request]
  (action))
