(ns jiksnu.views.user-views-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-format with-serialization]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        clj-tigase.core
        [jiksnu.actions.user-actions :only [index]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.helpers.user-helpers
        [jiksnu.ko :only [*dynamic*]]
        ;; jiksnu.views.user-views
        jiksnu.xmpp.element
        [midje.sweet :only [contains every-checker fact future-fact =>]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.user-actions :as actions.user]
            [net.cgrand.enlive-html :as enlive])
  (:import java.io.StringReader
           jiksnu.model.User))

(defn hiccup->doc
  [hiccup-seq]
  (-> hiccup-seq
      h/html
      StringReader.
      enlive/html-resource))

(test-environment-fixture

 (fact "apply-view #'index"
   (let [action #'index]
     (fact "when the serialization is :http"
      (with-serialization :http
        (fact "when the format is :html"
          (with-format :html
            (fact "when the request is not dynamic"
              (binding [*dynamic* false]
                (fact "when there are no activities"
                 (let [request {:action action}
                       response (filter-action action request)]

                   (apply-view request response) =>
                   (every-checker
                    map?
                    (fn [response]
                      (fact
                        (let [doc (hiccup->doc (:body response))]
                          (-> doc
                              (enlive/select [:.paginations-record-count
                                              :.pagination-value])
                              first
                              enlive/text) => (str 0)))))))))))))))
 
 (fact "apply-view #'show"
   (let [action #'actions.user/show]
     (fact "when the serialization is :xmpp"
       (with-serialization :xmpp
         (fact "when the format is :xmpp"
           (with-format :xmpp
             (let [user (model.user/create (factory :user))
                   request {:action action}
                   response (action user)]
               (apply-view request response) =>
               (every-checker
                map?
                (contains {:type :result})))))))))

 (fact "apply-view-test #'fetch-remote :xmpp"
   (let [action #'actions.user/fetch-remote]
     (fact "should return an iq query packet map"
       (with-context [:xmpp :xmpp]
         (let [user (model.user/create (factory :user))
               request {:action action}
               response (action user)]
          (apply-view request response) =>
          (every-checker
           map?
           (contains {:type :get})))))))
 )
