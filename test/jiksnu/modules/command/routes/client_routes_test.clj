(ns jiksnu.modules.command.routes.client-routes-test
  (:require [ciste.commands :refer [parse-command]]
            [ciste.core :refer [with-context]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [lamina.core :as l]
            [midje.sweet :refer [contains =>]]))

(test-environment-fixture

 (context #'parse-command

   (context "get-page"
     (let [name "get-page"]

       (context "clients"

         (context "when there are clients"
           (let [client (log/spy :info (mock/a-client-exists))]
             (let [ch (l/channel)
                   request {:channel ch
                            :name name
                            :format :json
                            :args (list "clients")}]
               (parse-command request) =>
               (check [response]
                 (log/spy :info response) => map?
                 (let [body (:body response)]
                   (let [json-obj (json/read-str body)]
                     json-obj => map?
                     )
                   )
                 )))))
       ))
   )
 )
