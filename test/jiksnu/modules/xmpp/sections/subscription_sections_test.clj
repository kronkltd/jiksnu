(ns jiksnu.modules.web.sections.subscription-sections-test
  (:use [jiksnu.sections.subscription-sections :only [subscriber-response-element
                                                      subscribe-request]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [jiksnu.mock :as mock]))

(test-environment-fixture

 (context #'subscriber-response-element
   (let [subscription (mock/a-subscription-exists)]
     (subscriber-response-element subscription) => vector?))

 (context #'subscribe-request
   (let [subscription (mock/a-subscription-exists)]
     (subscribe-request subscription) => vector?))

 )
