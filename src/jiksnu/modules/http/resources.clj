(ns jiksnu.modules.http.resources
  (:require [clojure.tools.logging :as log]
            [octohipster.core :as octo]
            [octohipster.routes :as octo-routes]))

(defonce groups
  ;; "Ref holding each api group"
  (ref []))

(defonce resources (ref {}))

(defn get-groups
  [site-var]
  (let [m (meta site-var)]
    (var-get (ns-resolve (:ns m) (symbol (str (:name m) "-groups"))))))

(defn add-group!
  [site group]
  (log/infof "adding group %s %s" site group)
  (dosync
   (alter (get-groups site) conj group)))

(defn get-resource
  [group-var resource-name]
  ;; TODO: implement
  )

(defn get-resources
  [group-var]
  (let [m (meta group-var)]
    (var-get (ns-resolve (:ns m) (symbol (str (:name m) "-resources"))))))

(defn add-resource!
  [group-var resource-name resource]
  (log/infof "adding resource %s(%s)" group-var resource-name)
  (dosync
   (alter (get-resources group-var) assoc resource-name resource)))

(defn get-route
  [site-var]
  (let [m (meta site-var)]
    (var-get (ns-resolve (:ns m) (symbol (str (:name m) "-routes"))))))

(defn init-site-reloading!
  [f]
  (add-watch
   resources
   :site (fn [k r os ns]
           (log/debug "refreshing site")
           (f))))

(defn update-groups
  [groups resources]
  (map
   (fn [gvar]
     (log/debug (str "Processing Group: " gvar))
     (let [options (log/spy :info (var-get gvar))
           group-resources (map val @(get-resources gvar))
           options (assoc options :resources (log/spy :info group-resources))]

       (octo/group options)))
   groups))

(defmacro defresource
  [group resource-name & {:as options}]
  (log/debugf "defining resource: %s(%s)" group resource-name)
  `(add-resource! (var ~group) ~resource-name (octo/resource ~options)))

(defmacro defgroup
  [group-name & {:as opts}]
  (let [resources-sym (symbol (str group-name "-resources"))]
    `(do
       (def ~group-name ~opts)
       (defonce ~resources-sym (ref {}))
       (dosync
        (alter groups conj (var ~group-name))))))

(defmacro defsite
  [site-name & {:as opts}]
  (let [route-sym    (symbol (str site-name "-routes"))
        resource-sym (symbol (str site-name "-resources"))
        group-sym    (symbol (str site-name "-groups"))
        init-sym     (symbol (str site-name "-init"))
        options (merge {:name (str site-name)
                        :groups @groups}
                       opts)]
    `(do
       (def ~site-name ~options)
       (declare ~route-sym)
       (defonce ~group-sym (ref []))
       (defonce ~resource-sym (ref []))
       (def ~init-sym (fn []
                        (let [groups# (update-groups @~group-sym @~resource-sym)
                              body# (assoc ~options :groups groups#)]
                          (log/debugf "Creating Site. Groups %s" (count groups#))
                          (let [routes# (octo-routes/routes body#)]
                            (def ~route-sym routes#)))))
       (~init-sym))))
