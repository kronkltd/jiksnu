(ns jiksnu.modules.rdf.sections.activity-sections-test
  (:use [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [index-block index-section]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            jiksnu.modules.rdf.sections.activity-sections)
  (:import jiksnu.model.Activity))

(test-environment-fixture

 (context #'index-block
   (context "when the context is [:http :rdf]"
     (with-context [:http :rdf]
       (let [activity (mock/there-is-an-activity)]
         (index-block [activity]) =>
         (check [response]
           response => (partial every? (fn [t]
                                         (and (vector? t)
                                              (= 3 (count t))))))))))

 (context #'index-section
   (context Activity
     (context "when the context is [:http :rdf]"
       (with-context [:http :rdf]
         (let [activity (mock/there-is-an-activity)]
           (index-section [activity]) =>
           (check [r]
             (doseq [t r]
               t => vector?
               (count t) => 3)))))))

 )
