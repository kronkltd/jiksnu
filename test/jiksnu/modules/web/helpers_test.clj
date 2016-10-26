(ns jiksnu.modules.web.helpers-test
  (:require [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import org.apache.http.HttpStatus))

(facts "#'jiksnu.modules.web.helpers/serve-template"
  (fact "when the template exists"
    (let [template-name "index-activities"
          request {:params {:* template-name}}]
      (helpers/serve-template request) =>
      (contains {:status HttpStatus/SC_OK})))
  (fact "when the template does not exist"
    (let [template-name "zzyzx"
          request {:params {:* template-name}}]
      (helpers/serve-template request) =>
      (contains {:status HttpStatus/SC_NOT_FOUND}))))
