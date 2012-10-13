(ns jiksnu.sections.user-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [uri show-section title]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        jiksnu.test-helper
        jiksnu.session
        jiksnu.sections.user-sections
        [midje.sweet :only [fact => every-checker]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.xmpp.element :as xmpp.element])
  (:import jiksnu.model.User))

(test-environment-fixture
 (fact "uri User :html :http"
   (fact "when the serialization is :http"
     (with-serialization :http
       (fact "when the format is :html"
         (with-format :html
           (fact "when it is html-only"
             (binding [*dynamic* false]
               (let [user (model.user/create (factory :user))]
                 (uri user) => string?))))))))

 (fact "title User"
   (fact "should return the title of that user"
     (with-context [:http :html]
       (let [user (model.user/create (factory :user))
             response (title user)]
         response => string?))))

 (fact "show-section User"
   (fact "when the serialization is :xmpp"
     (with-serialization :xmpp
       (fact "when the format is :xmpp"
         (with-format :xmpp
           (fact "should return a vcard string"
             (let [user (model.user/create (factory :user))]
               (show-section user) =>
               (every-checker
                #(fact % => #"<vcard")
                string?)))))))
   (fact "when the serialization is :http"
     (with-serialization :http
       (fact "when the format is :html"
         (with-format :html
           (binding [*dynamic* false]
            (let [user (model.user/create (factory :user))]
              (show-section user))) =>
             (every-checker
              (fn [response]
                (let [body (h/html response)]
                  (fact
                    body => #"user")))))))))
 )
