(ns jiksnu.modules.web.views.user-views-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-format with-serialization]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        clj-tigase.core
        [jiksnu.actions.user-actions :only [index]]
        [jiksnu.test-helper :only [check context future-context
                                   hiccup->doc test-environment-fixture]]
        [jiksnu.ko :only [*dynamic*]]
        jiksnu.modules.xmpp.element
        [midje.sweet :only [contains =>]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.views.user-views
            [net.cgrand.enlive-html :as enlive])
  (:import java.io.StringReader
           jiksnu.model.User))

(test-environment-fixture

 (context "apply-view #'index"
   (let [action #'index]
     (context "when the serialization is :http"
       (with-serialization :http
         (context "when the format is :html"
           (with-format :html
             (context "when the request is not dynamic"
               (binding [*dynamic* false]
                 (context "when there are no activities"
                   (let [request {:action action}
                         response (filter-action action request)]
                     (apply-view request response) => map?))))))))))

 )
