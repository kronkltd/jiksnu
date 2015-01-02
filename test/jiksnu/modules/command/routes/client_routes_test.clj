(ns jiksnu.modules.command.routes.client-routes-test
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

       (fact "clients"

         (fact "when there are clients"
           (let [client (mock/a-client-exists)]
             (let [ch (l/channel)
                   request {:channel ch
                            :name name
                            :format :json
                            :args (list "clients")}]
               (parse-command request) =>
               (check [response]
                 response => map?
                 (let [body (:body response)]
                   (let [json-obj (json/read-str body :key-fn keyword)]
                     json-obj => map?)))))))
       ))
   )
 )
