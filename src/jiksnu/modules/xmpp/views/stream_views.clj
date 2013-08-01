(ns jiksnu.modules.xmpp.views.stream-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section]]
        [jiksnu.actions.stream-actions :only [public-timeline user-timeline]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]))

(defview #'public-timeline :xmpp
  [request {:keys [items] :as page}]
  (tigase/result-packet request (index-section items page)))

(defview #'user-timeline :xmpp
  [request [user  activities]]
  (tigase/result-packet request (index-section activities)))
