(ns jiksnu.modules.core.helpers
  (:require [jiksnu.predicates :as predicates]
            [taoensso.timbre :as timbre]))

(defn try-resolve
  [route-sym fn-sym]
  (try
    (ns-resolve route-sym fn-sym)
    (catch Exception ex
      (timbre/error ex))))

(defn load-pages!
  [route-sym]
  (when-let [page-fn (try-resolve route-sym 'pages)]
    (when-let [matchers (page-fn)]
      (dosync
       (alter predicates/*page-matchers* concat matchers)))))

(defn load-sub-pages!
  [route-sym]
  (if-let [page-fn (try-resolve route-sym 'sub-pages)]
    (if-let [matchers (page-fn)]
      (dosync
       (alter predicates/*sub-page-matchers* concat matchers))
      (timbre/warn "No matchers returned"))
    #_(timbre/warnf "Could not load subpage function - %s" route-sym)))
