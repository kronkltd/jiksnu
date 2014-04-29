(ns jiksnu.viewmodel
  (:use [ciste.commands :only [add-command!]]
        [ciste.core :only [defaction]]
        [ciste.filters :only [deffilter filter-action]]
        [ciste.routes :only [resolve-routes]]
        [ciste.views :only [defview]]
        [jiksnu.predicates :as predicates]
        ;; [jiksnu.routes :only [http-routes]]
        ;; [jiksnu.routes.admin-routes :only [admin-routes]]
        )
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]))

(defaction fetch-viewmodel
  [path options]
  (let [request {:request-method :get
                 :uri            (str "/" path)
                 :params         options
                 :serialization  :http
                 :format         :viewmodel}
        response ((resolve-routes [predicates/http]
                                  []
                                  #_(concat admin-routes
                                            http-routes))
                  request)]
    (if-let [body (:body response)]
      (let [vm (json/read-str body :key-fn keyword)]
        {:path path
         :options options
         :body {:action "update viewmodel"
                :body vm}})
      {:body {:action "error"
              :body "could not find viewmodel"}})))

(deffilter #'fetch-viewmodel :command
  [action request]
  (let [[path opt-string] (:args request)]
    (action path opt-string)))

(defview #'fetch-viewmodel :json
  [request response]
  response)

(add-command! "fetch-viewmodel" #'jiksnu.viewmodel/fetch-viewmodel)
