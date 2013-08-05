(ns jiksnu.modules.atom.views.stream-views-test
  (:use [ciste.core :only [with-context with-serialization with-format
                           *serialization* *format*]]
        [ciste.formats :only [format-as]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [check context future-context hiccup->doc
                                   select-by-model test-environment-fixture]]
        [jiksnu.actions.stream-actions :only [public-timeline user-timeline]]
        [midje.sweet :only [=> contains truthy]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.atom.views.stream-views))

(test-environment-fixture

 (context "apply-view #'public-timeline"
   (let [action #'public-timeline]
     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :atom"
           (with-format :atom

             (context "when there are conversations"
               (db/drop-all!)
               (let [n 20
                     items (doall (for [i (range n)]
                                    (let [conversation (mock/a-conversation-exists)]
                                      (mock/there-is-an-activity {:conversation conversation})
                                      conversation)))
                     request {:action action}
                     response (filter-action action request)]

                 (apply-view request response) =>
                 (check [response]
                   response => map?
                   (:template response) => false
                   (let [formatted (format-as :atom request response)
                         feed (abdera/parse-xml-string (:body formatted))]
                     (count (.getEntries feed)) => n))))
             ))
         ))
     ))
 )
