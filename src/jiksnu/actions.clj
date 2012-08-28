(ns jiksnu.actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.core :only [defaction]]
        
        [ciste.filters :only [deffilter filter-action]]
        [ciste.views :only [defview]]
        )
  (:require [clojure.tools.logging :as log])
  )

(defaction invoke-action
  [model-name action-name id]
  (let [action-ns (symbol (str "jiksnu.actions." model-name "-actions"))]
    (require action-ns)

    (let [action (log/spy (ns-resolve (log/spy action-ns)
                                      (log/spy (symbol action-name))))]
      (let [body (filter-action action id)]
        {:message "action invoked"
         :model model-name
         :action action-name
         :id id
         :body body
         }))
    )

  )

(deffilter #'invoke-action :command
  [action request]
  (apply action (:args request))
  )

(defview #'invoke-action :json
  [request data]
  {:body data}
  )

(add-command! "invoke-action" #'invoke-action)
