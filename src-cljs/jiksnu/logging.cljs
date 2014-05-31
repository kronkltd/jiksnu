(ns jiksnu.logging)

(def console js/console)

(defn info
  [m]
  (.info console m))

(defn error
  [m]
  (.error console m))

(defn debug
  [m]
  (.debug console m))

(defn warn
  [m]
  (.warn console m))

(defn spy
  [m]
  (.info console m)
  m)

(defn spyc
  [m]
  (.info console (clj->js m))
  m)
