(ns jiksnu.helpers.user-helpers-test
  (:use (ciste [config :only [with-environment]])
        clj-factory.core
        midje.sweet
        (jiksnu test-helper model)
        jiksnu.helpers.user-helpers)
  (:require (jiksnu.actions [domain-actions :as actions.domain]
                            [user-actions :as actions.user])
            (jiksnu.model [user :as model.user]))
  (:import jiksnu.model.User))

(test-environment-fixture

 (background
  (around :facts
          (let [actor (factory User)]
            ?form)))

 (fact "fetch-user-meta"
   (fact "should return an xml stream"
     (let [user (actions.user/create (factory User {:domain "kronkltd.net"}))
           domain (model.user/get-domain user)]
       (actions.domain/update
        (assoc domain :links
               [{:rel "lrdd"
                 :template (str "http://" (:_id domain)
                                "/main/xrd?uri={uri}")}]))
       (fetch-user-meta user)) => nil)))

