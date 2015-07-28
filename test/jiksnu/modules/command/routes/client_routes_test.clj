(ns jiksnu.modules.command.routes.client-routes-test
  (:require [ciste.commands :refer [parse-command]]
            [ciste.core :refer [with-context]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [lamina.core :as l]
            [midje.sweet :refer :all]))

(test-environment-fixture

 (future-fact "parse command 'get-page clients'"
   (let [name "get-page"
         args '("clients")]

     (fact "when there are clients"
       (let [client (mock/a-client-exists)]
         (let [ch (l/channel)
               request {:channel ch
                        :name name
                        :format :json
                        :args args}]
           (let [response (parse-command request)]
             response => map?
             (let [body (:body response)]
               (let [json-obj (json/read-str body :key-fn keyword)]
                 json-obj => map?))))))
     ))
 )
