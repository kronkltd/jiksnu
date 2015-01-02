(ns jiksnu.modules.web.sections.user-sections-test
  (:use [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [uri show-section title]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.test-helper :only [check test-environment-fixture]]
        jiksnu.session
        [midje.sweet :only [=> fact]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(test-environment-fixture

 (fact "uri User :html :http"
   (fact "when the serialization is :http"
     (with-serialization :http
       (fact "when the format is :html"
         (with-format :html
           (fact "when it is html-only"
             (binding [*dynamic* false]
               (let [user (mock/a-user-exists)]
                 (uri user) => string?))))))))

 (fact "title User"
   (fact "should return the title of that user"
     (with-context [:http :html]
       (let [user (mock/a-user-exists)
             response (title user)]
         response => string?))))

 (fact "show-section User"
   (fact "when the serialization is :http"
     (with-serialization :http
       (fact "when the format is :html"
         (with-format :html
           (binding [*dynamic* false]
             (let [user (mock/a-user-exists)]
               (show-section user))) =>
               (check [response]
                 (let [body (h/html response)]
                   body => #"user")))))))
 )
