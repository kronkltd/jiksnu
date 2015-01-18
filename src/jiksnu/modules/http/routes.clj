(ns jiksnu.modules.http.routes)

(defonce groups
  ;; "Ref holding each api group"
  (ref {}))

(defonce resources (ref {}))
