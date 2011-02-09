(ns jiksnu.http.view.auth-view
  (:use jiksnu.http.controller.auth-controller
        jiksnu.http.view
        jiksnu.model
        jiksnu.view
        ciste.core
        ciste.view)
  (:require [hiccup.form-helpers :as f])
  (:import jiksnu.model.User))

(defview #'login :html
  [request id]
  {:session {:id id}
   :status 303
   :template false
   :headers {"Location" "/"}})

(defview #'logout :html
  [request successful]
  (if successful
    {:session {:id nil}
     :status 303
     :template false
     :headers {"Location" "/"}}))
