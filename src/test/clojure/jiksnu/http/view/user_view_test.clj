(ns jiksnu.http.view.user-view-test
  (:use ciste.core
        ciste.factory
        ciste.sections
        ciste.view
        jiksnu.http.view
        jiksnu.http.view.user-view
        jiksnu.model
        jiksnu.view
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(describe uri "User :html :http"
  (do-it "should return a link to that user"
    (with-format :html
      (with-serialization :http
        (let [user (model.user/create (factory User))]
          (let [response (uri user)]
            (expect (instance? String response))))))))

(describe title "User"
  (do-it "should return the title of that user"
    (with-format :html
      (with-serialization :http
        (let [user (model.user/create (factory User))]
          (let [response (title user)]
            (expect (instance? String response))))))))

(describe avatar-img
  (do-it "should return an image html"
    (with-format :html
      (with-serialization :http
        (let [user (model.user/create (factory User))]
          (let [response (avatar-img user)]
            (expect (vector? response))))))))

(describe display-minimal "User")

(describe index-table-line "User")

(describe add-form "User")

(describe edit-form "User")

(describe subscribe-form)

(describe unsubscribe-form)

(describe user-actions)

(describe show-section "User")

(describe apply-view "#'index :html")

(describe apply-view "#'show :html")

(describe apply-view "#'edit :html")

(describe apply-view "#'update :html")

(describe apply-view "#'delete :html")

