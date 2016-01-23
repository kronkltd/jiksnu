(ns jiksnu.modules.web.sections.user-sections-test
  (:require [ciste.core :refer [with-context with-format with-serialization]]
            [ciste.sections.default :refer [uri show-section title]]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            jiksnu.session
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "uri User :html :http"
  (fact "when the serialization is :http"
    (with-serialization :http
      (fact "when the format is :html"
        (with-format :html
          (let [user (mock/a-user-exists)]
            (uri user) => string?))))))

(fact "title User"
  (fact "should return the title of that user"
    (with-context [:http :html]
      (let [user (mock/a-user-exists)]
        (title user) => string?))))
