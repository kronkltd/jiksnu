(ns jiksnu.transforms.domain-transforms
  (:use [ciste.config :only [config]]))

(defn set-discovered
  [item]
  (if (contains? item :discovered)
    item
    (assoc item :discovered (:local item))))

(defn set-local
  [item]
  (if (contains? item :local)
    item
    (assoc item :local (= (:_id item) (config :domain)))))
