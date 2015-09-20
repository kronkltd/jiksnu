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
     (log/debugf "defining resource: %s" (var ~resource-name))
     (octo/defresource ~resource-name
       ~@opts)

     (dosync
      (alter resources assoc-in [(var ~group) (var ~resource-name)] ~resource-name))))


(defmacro defgroup
  [group-name & opts]
  `(do
     (declare ~group-name)
     (octo/defgroup ~group-name
       ~@opts)

     (dosync
      (alter groups conj (var ~group-name)))))

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
     (let [group (log/spy :info (var-get gvar))
           group-resources (map val (get resources gvar))]
       (doseq [r group-resources]
         (log/debug (str "- " (:id r))))
       (assoc group :resources group-resources)))
   groups))

(defmacro defsite
  [site-name & {:as opts}]
  (let [route-sym (symbol (str site-name "-routes"))
        resource-sym (symbol (str site-name "-resources"))
        group-sym (symbol (str site-name "-groups"))
        options (merge {:name (str site-name)
                        :groups @groups
                        } opts)]
    `(do
       (def ~site-name ~options)
       (declare ~route-sym)
       (defonce ~group-sym (ref []))
       (defonce ~resource-sym (ref []))
       (let [body ~(->> (update-groups (:groups options) resources)
                        (assoc options :groups)
                        (mapcat identity)
                        (log/spy :info))
             f# (fn []
                  (let [routes# (octo-routes/routes body)]
                    (def ~route-sym routes#)))]
         (init-site-reloading! f#)
         (f#)))))
