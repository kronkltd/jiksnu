(ns jiksnu.filters.activity-filters)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'index :xmpp
  [action request]
  (action))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Show
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'show :xmpp
  [action request]
  (let [{:keys [items]} request
        ids (map #(.getAttribute % "id") items)
        id (first ids)]
    (action id)))

(deffilter #'user-timeline :http
  [action request]
  (let [{{id "id"} :params} request]
    (action id)))

(deffilter #'delete :http
  [action request]
  (let [{{id "id"} :params} request]
    (action id)))
