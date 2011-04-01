(ns jiksnu.filters.activity-filters
  (:use ciste.filters
        jiksnu.controller.activity-controller))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'index :xmpp
  [action request]
  (action))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'show :http
  [action request]
  (let [{{id "id"} :params} request]
    (action id)))

(deffilter #'show :xmpp
  [action request]
  (let [{:keys [items]} request
        ids (map #(.getAttribute % "id") items)
        id (first ids)]
    (action id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; user-timeline
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'user-timeline :http
  [action request]
  (let [{{id "id"} :params} request]
    (action id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; delete
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'delete :http
  [action request]
  (let [{{id "id"} :params} request]
    (action id)))
