(ns jiksnu.modules.http.resources
  (:require [clojure.tools.logging :as log]
            [octohipster.core :as octo]
            [octohipster.routes :as octo-routes]))

(defonce groups
  ;; "Ref holding each api group"
  (ref {}))

(defonce resources (ref {}))

(defmacro defresource
  [group name & opts]
  `(do
     (declare ~name)
     (log/debugf "defining resource: %s" (var ~name))
     (octo/defresource ~name
       ~@opts)

     (dosync
      (alter resources assoc-in [(var ~group) (var ~name)] ~name))))


(defmacro defgroup
  [name & opts]
  `(do
     (declare ~name)
     (octo/defgroup ~name
       ~@opts)

     (dosync
      (alter groups assoc (var ~name) ~name))))

(defn set-site
  []

  )

(defn init-site-reloading!
  [f]
  (add-watch
   resources
   :site (fn [k r os ns]
           (log/info "refreshing site")
           (f))))

(defmacro defsite
  [site-name & {:as opts}]
  (let [route-sym (symbol (str site-name "-routes"))
        options (merge {:name (str site-name)} opts)]
    `(do
       (def ~site-name ~options)
       (let [f#
             (fn []
               (octo-routes/defroutes ~route-sym
                 ~@(log/spy :info (mapcat identity options))))]
         (init-site-reloading! f#)
         (f#)))))
