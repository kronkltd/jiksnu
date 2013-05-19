(ns jiksnu.views.stream-views-test
  (:use [ciste.core :only [with-context with-serialization with-format
                           *serialization* *format*]]
        [ciste.formats :only [format-as]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [hiccup->doc select-by-model test-environment-fixture]]
        [jiksnu.actions.stream-actions :only [public-timeline user-timeline]]
        [midje.sweet :only [every-checker fact future-fact => contains truthy]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.rdf :as rdf]
            [net.cgrand.enlive-html :as enlive]))

(test-environment-fixture

 (fact "apply-view #'public-timeline"
   (let [action #'public-timeline]
     (fact "when the serialization is :http"
       (with-serialization :http

         (fact "when the format is :atom"
           (with-format :atom

             (fact "when there are conversations"
               (db/drop-all!)
               (let [n 20
                     items (doall (for [i (range n)]
                                    (let [conversation (mock/a-conversation-exists)]
                                      (mock/there-is-an-activity {:conversation conversation})
                                      conversation)))
                     request {:action action}
                     response (filter-action action request)]

                 (apply-view request response) =>
                 (fn [response]
                   (fact
                     response => map?
                     (:template response) => false
                     (let [formatted (format-as :atom request response)
                           feed (abdera/parse-xml-string (:body formatted))]
                       (count (.getEntries feed)) => n)))))
             ))

         (fact "when the format is :html"
           (with-format :html

             (fact "when dynamic is false"
               (binding [*dynamic* false]

                 (fact "when there are conversations"
                   (db/drop-all!)
                   (let [n 20
                         items (doall (for [i (range n)] (mock/a-conversation-exists)))
                         request {:action action}
                         response (filter-action action request)]

                     (apply-view request response) =>
                     (fn [response]
                       (fact
                         response => map?
                         (let [resp-str (h/html (:body response))]
                           resp-str => string?)
                         (let [doc (hiccup->doc [:bogus (:body response)])]
                           (let [elts (select-by-model doc "conversation")]
                             (count elts) => n

                             (let [ids (->> elts
                                            (map #(get-in % [:attrs :data-id]))
                                            (into #{}))]
                               (count ids) => n
                               (doseq [item items]
                                 (let [id (str (:_id item))]
                                   ids => (contains id))))))))
                     ))
                 ))
             ))
         ))))

 (fact "apply-view #'user-timeline"
   (let [action #'user-timeline]
     (fact "when the serialization is :http"
       (with-serialization :http

         (fact "when the format is :html"
           (with-format :html
             (binding [*dynamic* false]
               (fact "when that user has activities"
                 (db/drop-all!)
                 (let [user (mock/a-user-exists)
                       activity (mock/there-is-an-activity {:user user})
                       request {:action action
                                :params {:id (str (:_id user))}}
                       response (filter-action action request)]
                   (apply-view request response) =>
                   (every-checker
                    (fn [response]
                      (let [doc (hiccup->doc (:body response))]
                        (fact
                          (map
                           #(get-in % [:attrs :data-id])
                           (enlive/select doc [(enlive/attr? :data-id)])) =>
                           (contains (str (:_id activity))))))))))))

         (fact "when the format is :n3"
           (with-format :n3
             (fact "when that user has activities"
               (db/drop-all!)
               (let [user (mock/a-user-exists)
                     activity (mock/there-is-an-activity {:user user})
                     request {:action action
                              :params {:id (str (:_id user))}}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (fn [response]
                    (fact
                      (let [body (:body response)]
                        body => (partial every? vector?)
                        (let [m (rdf/triples->model body)]
                          m => truthy)))))))))))))

 )
