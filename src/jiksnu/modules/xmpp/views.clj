(ns jiksnu.modules.xmpp.views
  (:use [ciste.core :only [serialize-as]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]))

(defmethod serialize-as :xmpp
  [serialization response]
  (when response
    (tigase/make-packet response)))
