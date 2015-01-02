(ns jiksnu.modules.web.views.stream-views-test
  (:require [ciste.core :refer [with-context with-serialization with-format
                                *serialization* *format*]]
            [ciste.formats :refer [format-as]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.stream-actions :refer [public-timeline user-timeline]]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.views.activity-views
            jiksnu.modules.web.views.conversation-views
            jiksnu.modules.web.views.stream-views
            [jiksnu.test-helper :refer [check hiccup->doc
                                        select-by-model test-environment-fixture]]
            [midje.sweet :refer [=> contains fact future-fact truthy]]
            [net.cgrand.enlive-html :as enlive]))

(test-environment-fixture

 (fact "apply-view #'public-timeline"
   (let [action #'public-timeline]
     (fact "when the serialization is :http"
       (with-serialization :http

         (fact "when the format is :html"
           (with-format :html

             (future-fact "when dynamic is false"
               (binding [*dynamic* false]

                 (fact "when there are conversations"
                   (db/drop-all!)
                   (let [n 20
                         items (doall (for [i (range n)] (mock/a-conversation-exists)))
                         activities (doall (for [item items]
                                             (mock/there-is-an-activity {:conversation item})))
                         request {:action action}
                         response (filter-action action request)
                         response (apply-view request response)]

                     (fact "returns a map"
                       response => map?)

                     (fact "has a body of type string"
                       (let [resp-str (h/html (:body response))]
                         resp-str => string?))

                     (fact "contains the ids"
                       (let [doc (hiccup->doc [:bogus (:body response)])]
                         (let [elts (enlive/select doc [:.conversation-section])]
                           (count elts) => n

                           (let [ids (->> elts
                                          (map #(get-in % [:attrs :data-id]))
                                          (into #{}))]
                             (count ids) => n
                             (doseq [item items]
                               (let [id (str (:_id item))]
                                 ids => (contains id)))))
                         (let [elts (select-by-model doc "activity")]
                           (count elts) => n)))))
                 ))
             ))
         ))))

 (fact "apply-view #'user-timeline"
   (let [action #'user-timeline]
     (fact "when the serialization is :http"
       (with-serialization :http

         (fact "when the format is :html"
           (with-format :html

             (future-fact "when in static mode"
               (binding [*dynamic* false]
                 (fact "when that user has activities"
                   (db/drop-all!)
                   (let [user (mock/a-user-exists)
                         activity (mock/there-is-an-activity {:user user})
                         request {:action action
                                  :params {:id (str (:_id user))}}
                         response (filter-action action request)]
                     (apply-view request response) =>
                     (check [response]
                            (let [doc (hiccup->doc (:body response))]
                              (map
                               #(get-in % [:attrs :data-id])
                               (enlive/select doc [(enlive/attr? :data-id)])) =>
                               (contains (str (:_id activity)))))))))))

         ))))

 )
