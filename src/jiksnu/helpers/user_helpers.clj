(ns jiksnu.helpers.user-helpers
  (:use [clojure.core.incubator :only [-?>]]
        [jiksnu.session :only [current-user]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.namespace :as ns]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger])
  (:import javax.xml.namespace.QName
           jiksnu.model.User
           org.apache.abdera2.model.Entry
           tigase.xml.Element))

