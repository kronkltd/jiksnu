(ns jiksnu.modules.xmpp.views.user-views-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-format with-serialization]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.actions.user-actions :only [index]]
        [jiksnu.test-helper :only [check context future-context
                                   hiccup->doc test-environment-fixture]]
        [jiksnu.ko :only [*dynamic*]]
        jiksnu.modules.xmpp.element
        [midje.sweet :only [contains =>]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.xmpp.views.user-views :as views.user])
  (:import java.io.StringReader
           jiksnu.model.User))

(test-environment-fixture
 (context "apply-view #'show"
   (let [action #'actions.user/show]
     (context "when the serialization is :xmpp"
       (with-serialization :xmpp
         (context "when the format is :xmpp"
           (with-format :xmpp
             (let [user (mock/a-user-exists)
                   request {:action action}
                   response (action user)]
               (apply-view request response) =>
               (check [response]
                 response => map?
                 response => (contains {:type :result})))))))))

 (future-context "apply-view-test #'fetch-remote :xmpp"
   (let [action #'actions.user/fetch-remote]
     (context "should return an iq query packet map"
       (with-context [:xmpp :xmpp]
         (let [user (mock/a-user-exists)
               request {:action action}
               response (action user)]
           (apply-view request response) =>
           (check [response]
             response => map?
             response => (contains {:type :get})))))))

 )
