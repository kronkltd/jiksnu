(ns jiksnu.routes.like-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker contains]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "show-http-route"
   (let [tag-name (fseq :word)]
     (-> (mock/request :get (str "/tags/" tag-name))
         response-for) =>
     (every-checker
      (contains {:status 200}))))

 )
