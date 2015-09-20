(ns jiksnu.modules.http.resources
  (:require [clojure.tools.logging :as log]
            [octohipster.core :as octo]
            [octohipster.routes :as octo-routes]))

(defonce groups
  ;; "Ref holding each api group"
  (ref []))

(defonce resources (ref {}))

(defmacro defresource
  [group resource-name & opts]
  `(do
     (declare ~resource-name)
     #_(log/debugf "defining resource: %s(%s)" ~group ~resource-name)
     (octo/defresource ~resource-name
       ~@opts)

     (dosync
      (alter resources assoc-in [(var ~group) (var ~resource-name)] ~resource-name))))


(defmacro defgroup
  [group-name & opts]
  (let [resources-sym (symbol (str group-name "-resources"))]
    `(do
       (declare ~group-name)
       (defonce ~resources-sym (ref []))
       #_(octo/defgroup ~group-name
           ~@opts)

            (dosync
             (alter groups conj (var ~group-name))))))

(defn init-site-reloading!
  [f]
  (add-watch
   resources
   :site (fn [k r os ns]
           (log/info "refreshing site")
           (f))))

(defn update-groups
  [groups resources]
  (map
   (fn [gvar]
     (log/debug (str "Processing Group: " gvar))
     (let [options (log/spy :info (var-get gvar))
           group-resources (map val (get resources gvar))
           options (assoc options :resources group-resources)]
       (octo/group options)))
   groups))

(defn add-group!
  [site group]
  (log/infof "adding group %s %s" site group))

(defmacro defsite
  [site-name & {:as opts}]
  (let [route-sym (symbol (str site-name "-routes"))
        resource-sym (symbol (str site-name "-resources"))
        group-sym (symbol (str site-name "-groups"))
        options (merge {:name (str site-name)
                        :groups @groups}
                       opts)]
    `(do
       (def ~site-name ~options)
       (declare ~route-sym)
       (defonce ~group-sym (ref []))
       (defonce ~resource-sym (ref []))
       (let [f# (fn []
                  (let [body# (assoc ~options :groups (update-groups @~group-sym @~resource-sym))
                        routes# (octo-routes/routes body#)]
                    (def ~route-sym routes#)))]
         (init-site-reloading! f#)
         (f#)))))
