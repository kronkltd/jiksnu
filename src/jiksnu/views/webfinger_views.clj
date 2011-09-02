(ns jiksnu.views.webfinger-views
  (:use (ciste config core views)
        jiksnu.actions.webfinger-actions)
  (:require (hiccup [core :as h])
            (jiksnu.model [signature :as model.signature]
                          [webfinger :as model.webfinger])))

(defview #'host-meta :html
  [request _]
  (let [domain (config :domain)]
    {:template false
     :headers {"Content-Type" "application/xrds+xml"
               "Access-Control-Allow-Origin" "*"}
     :body (h/html (model.webfinger/host-meta domain))}))

(defview #'user-meta :html
  [request user]
  {:template false
   :headers {"Content-Type" "application/xrds+xml"
             "Access-Control-Allow-Origin" "*"}
   :body (h/html (model.webfinger/user-meta user))})
