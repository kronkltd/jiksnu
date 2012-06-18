(ns jiksnu.actions.like-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.like-actions :only [show]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker contains]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [ring.mock.request :as mock]))

(test-environment-fixture

 ;; (fact "#'show"
 ;;   (let [tag-name (fseq :word)]
 ;;     (show tag-name) =>
 ;;     (every-checker
 ;;      seq?)))
 
 )
