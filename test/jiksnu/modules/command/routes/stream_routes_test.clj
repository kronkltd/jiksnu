(ns jiksnu.modules.command.routes.stream-routes-test
  (:require [ciste.commands :refer [parse-command]]
            [ciste.core :refer [with-context]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [lamina.core :as l]
            [midje.sweet :refer [contains => fact]]))

(test-environment-fixture
 (fact #'parse-command
   (fact "get-page"
     (let [name "get-page"]

       (fact "streams"
         (let [ch (l/channel)
               request {:name name
                              :channel ch
                              :format :json
                              :args (list "streams")}]
                 (parse-command request) =>
                 (check [response]
                   response => map?
                   (let [body (:body response)]
                     body => string?
                     (let [response-obj (json/read-str body)]
                       response-obj => map?
                       )
                     )
                   )
                 ))))
   )
 )
