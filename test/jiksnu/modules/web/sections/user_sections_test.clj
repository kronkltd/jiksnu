(ns jiksnu.modules.web.sections.user-sections-test
  (:require [ciste.core :refer [with-context with-format with-serialization]]
            [ciste.sections.default :refer [uri show-section title]]
            [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            jiksnu.session
            [midje.sweet :refer [=> fact future-fact]])
  (:import jiksnu.model.User))

(test-environment-fixture

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
       (let [user (mock/a-user-exists)
             response (title user)]
         response => string?))))

 )
