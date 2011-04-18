(jiksnu.views.user-views-test
 (:use ciste.core
       ciste.factory
       ciste.sections
       ciste.views
       clj-tigase.core
       jiksnu.model
       jiksnu.view
       jiksnu.view.user-views
       jiksnu.xmpp.element
       [lazytest.describe :only (describe testing do-it)]
       [lazytest.expect :only (expect)])
 (:require [jiksnu.model.user :as model.user]
           [jiksnu.actions.user-actions :as actions.user])
 (:import jiksnu.model.User))

(describe get-uri)

(describe author-uri)

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

(describe show-section "User :xmpp :xmpp"
  (do-it "should return an element"
    (with-serialization :xmpp
      (with-format :xmpp
        (let [user (model.user/create (factory User))]
          (let [response (show-section user)]
            (expect (element? response))))))))

(describe apply-view "#'show :xmpp"
  (do-it "should return a query results packet map"
    (with-format :xmpp
      (with-serialization :xmpp
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to (make-jid user)
                       :from (make-jid user)
                       :type :get})
              request (merge {:format :xmpp
                              :action #'actions.user/show}
                             (make-request packet))]
          (let [response (apply-view request user)]
            (expect (map? response))
            (expect (= :result (:type response)))))))))

(describe apply-view "#'fetch-remote :xmpp"
  (do-it "should return an iq query packet map"
    (with-format :xmpp
      (with-serialization :xmpp
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to (make-jid user)
                       :from (make-jid user)
                       :type :get})
              request (merge {:format :xmpp
                              :action #'actions.user/fetch-remote}
                             (make-request packet))]
          (let [response (apply-view request user)]
            (expect (map? response))
            (expect (= :get (:type response)))))))))

(describe apply-view "#'remote-create :xmpp"
  (do-it "should return a query results packet map"
    (with-format :xmpp
      (with-serialization :xmpp
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to (make-jid user)
                       :from (make-jid user)
                       :type :get})
              request (merge {:format :xmpp
                              :action #'actions.user/remote-create}
                             (make-request packet))]
          (let [response (apply-view request user)]
            (expect (map? response))
            (expect (= :result (:type response)))))))))
