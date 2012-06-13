(ns jiksnu.views.user-views-test
  (:use [ciste.config :only [with-environment]]
        ciste.core
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        clj-tigase.core
        [jiksnu.actions.user-actions :only [index]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.helpers.user-helpers
        jiksnu.views.user-views
        jiksnu.xmpp.element
        [midje.sweet :only [contains every-checker fact future-fact =>]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [hiccup.core :as h]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.user-actions :as actions.user])
  (:import jiksnu.model.User))

(test-environment-fixture

 (fact "apply-view #'index"
   (let [action #'index]
     (fact "when the serialization is :http"
      (with-serialization :http
        (fact "when the format is :html"
          (with-format :html
            (fact "when there are no activities"
              (let [request {:action action
                             :serialization *serialization*
                             :format *format*}
                    response (filter-action action request)]

                (apply-view request response) =>
                (every-checker
                 map?
                 (fn [response]
                   
                   (let [body (h/html (:body response))]
                     (fact
                       body => #"Total Records: 0"))))))))))))
 
 (fact "apply-view #'show :xmpp"
   (fact "should return a query results packet map"
     (with-context [:xmpp :xmpp]
       (let [user (model.user/create (factory User))
             packet (tigase/make-packet
                     {:to (tigase/make-jid user)
                      :from (tigase/make-jid user)
                      :type :get})
             request (merge {:format :xmpp
                             :action #'actions.user/show}
                            (packet/make-request packet))]
         (let [response (apply-view request user)]
           response => map?
           response => (contains {:type :result}))))))

 (fact "apply-view-test #'fetch-remote :xmpp"
   (fact "should return an iq query packet map"
     (with-context [:xmpp :xmpp]
       (let [user (model.user/create (factory User))
             packet (tigase/make-packet
                     {:to (tigase/make-jid user)
                      :from (tigase/make-jid user)
                      :type :get})
             request (merge {:format :xmpp
                             :action #'actions.user/fetch-remote}
                            (packet/make-request packet))]
         (let [response (apply-view request user)]
           response => map?
           response => (contains {:type :get}))))))

 (future-fact "apply-view #'remote-create :xmpp"
   (fact "should return a query results packet map"
     (with-context [:xmpp :xmpp]
       (let [user (model.user/create (factory User))
             packet (tigase/make-packet
                     {:to (tigase/make-jid user)
                      :from (tigase/make-jid user)
                      :type :get})
             request (merge {:format :xmpp
                             :action #'actions.user/remote-create}
                            (packet/make-request packet))]
         (let [response (apply-view request user)]
           response => map?
           (-> response :type :result) => truthy))))))
