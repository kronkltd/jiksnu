(ns jiksnu.views.user-views-test
  (:use (ciste [config :only [with-environment]]
               core
               views)
        ciste.sections.default
        clj-factory.core
        clj-tigase.core
        (jiksnu test-helper
                model)
        jiksnu.helpers.user-helpers
        jiksnu.views.user-views
        jiksnu.xmpp.element
        midje.sweet)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu.model [user :as model.user])
            (jiksnu.actions [user-actions :as actions.user]))
  (:import jiksnu.model.User))

(test-environment-fixture

 (fact "uri User :html :http"
   (fact "should return a link to that user"
     (with-context [:http :html]       
       (let [user (model.user/create (factory User))]
         (uri user) => string?))))

 (fact "title User"
   (fact "should return the title of that user"
     (with-context [:http :html]
       (let [user (model.user/create (factory User))
             response (title user)]
         response => string?))))

 (fact "show-section User :xmpp :xmpp"
   (fact "should return an element"
     (with-context [:xmpp :xmpp]
       (let [user (model.user/create (factory User))]
         (show-section user) => element/element?))))

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

 (fact "apply-view #'remote-create :xmpp"
   (future-fact "should return a query results packet map"
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
