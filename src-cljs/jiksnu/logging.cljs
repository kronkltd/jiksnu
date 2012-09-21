(ns jiksnu.logging)

(defn info
  [m]
  (.info js/console m))

(defn error
  [m]
  (.error js/console m))

(defn debug
  [m]
  (.debug js/console m))
