(jiksnu.views.user-views-test
 (:use ciste.core
       ciste.sections
       ciste.views
       clj-factory.core
       clj-tigase.core
       jiksnu.model
       jiksnu.view
       jiksnu.view.user-views
       jiksnu.xmpp.element)
 (:require [jiksnu.model.user :as model.user]
           [jiksnu.actions.user-actions :as actions.user])
 (:import jiksnu.model.User))

(deftest get-uri)

(deftest author-uri)

(deftest uri "User :html :http"
  (testing "should return a link to that user"
    (with-format :html
      (with-serialization :http
        (let [user (model.user/create (factory User))]
          (let [response (uri user)]
            (expect (instance? String response))))))))

(deftest title "User"
  (testing "should return the title of that user"
    (with-format :html
      (with-serialization :http
        (let [user (model.user/create (factory User))]
          (let [response (title user)]
            (expect (instance? String response))))))))

(deftest avatar-img
  (testing "should return an image html"
    (with-format :html
      (with-serialization :http
        (let [user (model.user/create (factory User))]
          (let [response (avatar-img user)]
            (expect (vector? response))))))))

(deftest display-minimal "User")

(deftest index-table-line "User")

(deftest add-form "User")

(deftest edit-form "User")

(deftest subscribe-form)

(deftest unsubscribe-form)

(deftest user-actions)

(deftest show-section "User")

(deftest apply-view "#'index :html")

(deftest apply-view "#'show :html")

(deftest apply-view "#'edit :html")

(deftest apply-view "#'update :html")

(deftest apply-view "#'delete :html")

(deftest show-section "User :xmpp :xmpp"
  (testing "should return an element"
    (with-serialization :xmpp
      (with-format :xmpp
        (let [user (model.user/create (factory User))]
          (let [response (show-section user)]
            (expect (element? response))))))))

(deftest apply-view "#'show :xmpp"
  (testing "should return a query results packet map"
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

(deftest apply-view "#'fetch-remote :xmpp"
  (testing "should return an iq query packet map"
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

(deftest apply-view "#'remote-create :xmpp"
  (testing "should return a query results packet map"
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
